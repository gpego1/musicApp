package br.com.project.music.services.impl;
import br.com.project.music.business.dtos.UserDTO;
import br.com.project.music.business.entities.Musico;
import br.com.project.music.business.entities.User;
import br.com.project.music.business.repositories.UserRepository;
import br.com.project.music.services.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;
import org.hibernate.StaleObjectStateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
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
        try {
            if (userDTO.getName() != null && !userDTO.getName().equals(existingUser.getName())) {
                existingUser.setName(userDTO.getName());
            }
            if (userDTO.getEmail() != null && !userDTO.getEmail().equals(existingUser.getEmail())) {
                existingUser.setEmail(userDTO.getEmail());
            }
            if (userDTO.getSenha() != null && !passwordEncoder.matches(userDTO.getSenha(), existingUser.getSenha())) {
                existingUser.setSenha(passwordEncoder.encode(userDTO.getSenha()));
            }
            if (userDTO.getDataCriacao() != null && !userDTO.getDataCriacao().equals(existingUser.getDataCriacao())) {
                existingUser.setDataCriacao(userDTO.getDataCriacao());
            }
            if (userDTO.getRole() != null && !userDTO.getRole().equals(existingUser.getRole())) {
                existingUser.setRole(userDTO.getRole());
            }
            return convertToDTO(userRepository.save(existingUser));
        } catch (StaleObjectStateException ex) {
            throw new OptimisticLockException("The data you were trying to update has been modified by another user. Please refresh the data and try again.");
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

    private UserDTO convertToDTO(User user) {
       UserDTO dto = new UserDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                null,
                user.getDataCriacao(),
                user.getRole(),
                user.getMusico() != null ? user.getMusico().getNomeArtistico() : null,
                user.getMusico() != null ? user.getMusico().getRedesSociais() : null
        );
        return dto;
    }
}