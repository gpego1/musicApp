package br.com.project.music.services.impl;
import br.com.project.music.business.dtos.UserDTO;
import br.com.project.music.business.entities.Musico;
import br.com.project.music.business.entities.User;
import br.com.project.music.business.repositories.UserRepository;
import br.com.project.music.exceptions.OptimisticLockException;
import br.com.project.music.services.UserService;
import jakarta.persistence.EntityNotFoundException;

import org.hibernate.StaleObjectStateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Value("${file.upload-dir}")
    private String uploadDirectory;

    @Override
    public UserDTO createUser(UserDTO userDTO) {
        User user = new User();
        user.setName(userDTO.getName());
        user.setEmail(userDTO.getEmail());
        user.setSenha(passwordEncoder.encode(userDTO.getSenha()));
        user.setDataCriacao(Timestamp.from(Instant.now()));
        user.setRole(userDTO.getRole() != null ? userDTO.getRole() : User.Role.CLIENT);

        if(userDTO.getRole() == User.Role.ARTISTA) {
            Musico musico = new Musico();
            musico.setNomeArtistico(userDTO.getNomeArtistico());
            musico.setRedesSociais(userDTO.getRedesSociais());
            user.setMusico(musico);
        }

        User savedUser = userRepository.save(user);
        return convertToDTO(savedUser);
    }

    @Override
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserDTO getUserById(Long id) {
        return userRepository.findById(id)
                .map(this::convertToDTO)
                .orElse(null);
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User getUserByEmailAndSenha(String email, String senha) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (passwordEncoder.matches(senha, user.getSenha())) {
                return user;
            }
        }
        throw new EntityNotFoundException("Invalid email or password");
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario nao foi encotrado " + email));

        System.out.println("Carregando usuário por email: " + email + ", ID: " + user.getId());

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getSenha())
                .authorities(user.getRole().name())
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .build();
    }
    @Override
    @Transactional
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (userDTO.getName() != null) {
            existingUser.setName(userDTO.getName());
        }
        if (userDTO.getEmail() != null) {
            existingUser.setEmail(userDTO.getEmail());
        }
        if (userDTO.getSenha() != null) {
            existingUser.setSenha(passwordEncoder.encode(userDTO.getSenha()));
        }

        if (userDTO.getRole() != null && !userDTO.getRole().equals(existingUser.getRole())) {
            validateRoleChange(existingUser.getRole(), userDTO.getRole());
            existingUser.setRole(userDTO.getRole());


            if (userDTO.getRole() == User.Role.ARTISTA && existingUser.getMusico() == null) {
                Musico musico = new Musico();
                musico.setUsuario(existingUser);
                existingUser.setMusico(musico);
            }
        }

        User updatedUser = userRepository.save(existingUser);
        return convertToDTO(updatedUser);
    }
    private void validateRoleChange(User.Role currentRole, User.Role newRole) {
        if (currentRole != User.Role.CLIENT) {
            throw new IllegalStateException("Only CLIENT users can change their role");
        }
        if (newRole == User.Role.CLIENT) {
            throw new IllegalArgumentException("Cannot change to same role");
        }
        if (newRole != User.Role.HOST && newRole != User.Role.ARTISTA) {
            throw new IllegalArgumentException("Invalid target role");
        }
    }

    @Override
    @Transactional
    public User registerOrLoginGoogleUser(OAuth2User oauthUser) {
        String email = oauthUser.getAttribute("email");
        String googleId = oauthUser.getAttribute("sub");
        String name = oauthUser.getAttribute("name");
        String picture = oauthUser.getAttribute("picture");

        if (email == null || googleId == null) {
            throw new IllegalArgumentException("Missing required Google OAuth2 attributes");
        }
        Optional<User> existingUserByGoogleId = userRepository.findByGoogleId(googleId);
        if (existingUserByGoogleId.isPresent()) {
            User user = existingUserByGoogleId.get();
            if (name != null && !name.equals(user.getName())) {
                user.setName(name);
            }
            if (picture != null && !picture.equals(user.getFoto())) {
                user.setFoto(picture);
            }
            return userRepository.save(user);
        }

        Optional<User> existingUserByEmail = userRepository.findByEmail(email);
        if (existingUserByEmail.isPresent()) {
            User user = existingUserByEmail.get();
            if (user.getGoogleId() == null) {
                user.setGoogleId(googleId);
                user.setFoto(picture);
                return userRepository.save(user);
            } else if (!user.getGoogleId().equals(googleId)) {
                throw new IllegalStateException("Email already associated with different Google account");
            }
            return user;
        }
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setName(name != null ? name : email.split("@")[0]);
        newUser.setGoogleId(googleId);
        newUser.setFoto(picture);
        newUser.setDataCriacao(Timestamp.from(Instant.now()));
        newUser.setRole(User.Role.CLIENT);
        newUser.setSenha(passwordEncoder.encode(UUID.randomUUID().toString()));
        return userRepository.save(newUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
    @Override
    public boolean checkPassword(User user, String currentPassword) {
        return passwordEncoder.matches(currentPassword, user.getSenha());
    }

    @Override
    public void changePassword(User user, String newPassword) {
        user.setSenha(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public String uploadProfileImage(Long userId, MultipartFile file) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com ID: " + userId));

        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path uploadPath = Paths.get(uploadDirectory);

        if(!Files.exists(uploadPath)){
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        user.setFoto(fileName);
        user.setProfilePictureContentType(file.getContentType());
        userRepository.save(user);
        return fileName;
    }
    private UserDTO convertToDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                null,
                user.getDataCriacao(),
                user.getRole(),
                user.getMusico() != null ? user.getMusico().getNomeArtistico() : null,
                user.getMusico() != null ? user.getMusico().getRedesSociais() : null,
                user.getFoto()
        );
    }
}