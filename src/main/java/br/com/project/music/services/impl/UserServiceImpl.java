package br.com.project.music.services.impl;
import br.com.project.music.business.dtos.UserDTO;
import br.com.project.music.business.entities.Musico;
import br.com.project.music.business.entities.User;
import br.com.project.music.business.repositories.UserRepository;
import br.com.project.music.services.S3Service;
import br.com.project.music.services.UserService;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import jakarta.persistence.EntityNotFoundException;
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
import java.net.URL;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
public class UserServiceImpl implements UserService {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AmazonS3 s3Client;
    private final S3Service s3Service;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, AmazonS3 s3Client, S3Service s3Service) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.s3Client = s3Client;
        this.s3Service = s3Service;
    }

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.profile-images-dir}")
    private String profileImagesDir;

    @Override
    public UserDTO createUser(UserDTO userDTO) {

        if (userDTO.getEmail() == null || userDTO.getEmail().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }

        if (userRepository.findByEmail(userDTO.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email já cadastrado: " + userDTO.getEmail());
        }
        User user = new User();
        user.setName(userDTO.getName());
        user.setEmail(userDTO.getEmail());
        user.setSenha(passwordEncoder.encode(userDTO.getSenha()));
        user.setDataCriacao(Timestamp.from(Instant.now()));
        user.setFcmToken(userDTO.getFcmToken());
        user.setBio(userDTO.getBio());
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

        if (userDTO.getFcmToken() != null) {
            existingUser.setFcmToken(userDTO.getFcmToken());
        }

        if (userDTO.getSenha() != null) {
            existingUser.setSenha(passwordEncoder.encode(userDTO.getSenha()));
        }
        if (userDTO.getBio() != null) {
            existingUser.setBio(userDTO.getBio());
        }
        if (userDTO.getRole() != null && !userDTO.getRole().equals(existingUser.getRole())) {
            validateRoleChange(existingUser.getRole(), userDTO.getRole());
            existingUser.setRole(userDTO.getRole());

            if (userDTO.getRole() == User.Role.ARTISTA) {
                Musico musico = existingUser.getMusico();
                if (musico == null) {
                    musico = new Musico();
                    musico.setUsuario(existingUser);
                    existingUser.setMusico(musico);
                }
                if (userDTO.getNomeArtistico() != null) {
                    musico.setNomeArtistico(userDTO.getNomeArtistico());
                }
                if (userDTO.getRedesSociais() != null) {
                    musico.setRedesSociais(userDTO.getRedesSociais());
                }
            } else if (existingUser.getRole() == User.Role.ARTISTA && userDTO.getRole() != User.Role.ARTISTA) {
                existingUser.setMusico(null);
            }
        } else if (userDTO.getRole() == User.Role.ARTISTA) {
            Musico musico = existingUser.getMusico();
            if (musico != null) {
                if (userDTO.getNomeArtistico() != null) {
                    musico.setNomeArtistico(userDTO.getNomeArtistico());
                }
                if (userDTO.getRedesSociais() != null) {
                    musico.setRedesSociais(userDTO.getRedesSociais());
                }
            }
        }
        if (userDTO.getProfileCompleted() != null) {
            existingUser.setProfileCompleted(userDTO.getProfileCompleted());
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
        String googleOriginalPictureUrl = oauthUser.getAttribute("picture");

        if (email == null || googleId == null) {
            logger.error("Dados do Google incompletos: e-mail ou 'sub' ausentes.");
            throw new IllegalArgumentException("Atributos Google OAuth2 obrigatórios ausentes (email ou sub)");
        }

        Optional<User> existingUserByGoogleId = userRepository.findByGoogleId(googleId);
        if (existingUserByGoogleId.isPresent()) {
            User user = existingUserByGoogleId.get();

            if (name != null && !name.equals(user.getName())) {
                user.setName(name);
            }

            if (user.getSenha() == null || user.getSenha().isEmpty()) {
                user.setSenha(passwordEncoder.encode(UUID.randomUUID().toString()));
                logger.info("Gerada senha aleatória para usuário existente {} ao vincular Google.", user.getEmail());
            }

            boolean googleUrlChanged = (googleOriginalPictureUrl != null && !googleOriginalPictureUrl.equals(user.getGoogleProfilePictureUrl()));
            boolean s3UrlMissing = (user.getGoogleProfilePictureUrlS3() == null || user.getGoogleProfilePictureUrlS3().isEmpty());

            if (googleUrlChanged) {
                user.setGoogleProfilePictureUrl(googleOriginalPictureUrl);
            }

            if (googleOriginalPictureUrl != null && (googleUrlChanged || s3UrlMissing)) {
                String s3FileName = "google_" + googleId + "_" + UUID.randomUUID().toString().substring(0, 8) + ".jpg";
                String uploadedS3Url = s3Service.uploadImageFromUrl(googleOriginalPictureUrl, s3FileName);
                if (uploadedS3Url != null) {
                    user.setGoogleProfilePictureUrlS3(uploadedS3Url);
                    logger.info("Foto de perfil do S3 atualizada para usuário Google existente {}: {}", user.getEmail(), uploadedS3Url);
                } else {
                    logger.warn("Falha ao fazer upload da foto de perfil Google atualizada para o usuário: {}", user.getEmail());
                }
            }
            return userRepository.save(user);
        }

        Optional<User> existingUserByEmail = userRepository.findByEmail(email);
        if (existingUserByEmail.isPresent()) {
            User user = existingUserByEmail.get();
            if (user.getGoogleId() == null) {
                user.setGoogleId(googleId);
                user.setGoogleProfilePictureUrl(googleOriginalPictureUrl);

                if (googleOriginalPictureUrl != null) {
                    String s3FileName = "google_" + googleId + "_" + UUID.randomUUID().toString().substring(0, 8) + ".jpg";
                    String uploadedS3Url = s3Service.uploadImageFromUrl(googleOriginalPictureUrl, s3FileName);
                    if (uploadedS3Url != null) {
                        user.setGoogleProfilePictureUrlS3(uploadedS3Url);
                        logger.info("Foto de perfil do S3 carregada para usuário vinculado {}: {}", user.getEmail(), uploadedS3Url);
                    } else {
                        logger.warn("Falha ao carregar a foto de perfil do Google para o usuário vinculado: {}", user.getEmail());
                    }
                }
                if (user.getRole() == null ) {
                    user.setRole(User.Role.CLIENT);
                }
                return userRepository.save(user);
            } else if (!user.getGoogleId().equals(googleId)) {
                logger.error("Conflito de e-mail: e-mail {} já associado a um Google ID diferente.", email);
                throw new IllegalStateException("E-mail já associado a uma conta Google diferente");
            }
            return user;
        }
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setName(name != null ? name : email.split("@")[0]);
        newUser.setGoogleId(googleId);
        newUser.setGoogleProfilePictureUrl(googleOriginalPictureUrl);

        if (googleOriginalPictureUrl != null) {
            String s3FileName = "google_" + googleId + "_" + UUID.randomUUID().toString().substring(0, 8) + ".jpg";
            String uploadedS3Url = s3Service.uploadImageFromUrl(googleOriginalPictureUrl, s3FileName);
            if (uploadedS3Url != null) {
                newUser.setGoogleProfilePictureUrlS3(uploadedS3Url);
                logger.info("Foto de perfil do S3 carregada para novo usuário Google {}: {}", newUser.getEmail(), uploadedS3Url);
            } else {
                logger.warn("Falha ao carregar a foto de perfil do Google para o novo usuário: {}", newUser.getEmail());
            }
        }
        newUser.setFoto(null);
        newUser.setDataCriacao(Timestamp.from(Instant.now()));
        newUser.setRole(User.Role.CLIENT);
        newUser.setSenha(passwordEncoder.encode(UUID.randomUUID().toString()));
        return userRepository.save(newUser);
    }

    @Transactional
    public User findOrCreateGoogleUser(String email, String googleId) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User(email, User.Role.CLIENT, googleId);
            return userRepository.save(newUser);
        });
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

        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if(originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String newFilename = UUID.randomUUID().toString() + fileExtension;
        String finalBasePath = (profileImagesDir != null && !profileImagesDir.isEmpty() && !profileImagesDir.endsWith("/"))
                ? profileImagesDir + "/"
                : profileImagesDir;
        String s3Key = finalBasePath + newFilename;
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    bucketName,
                    s3Key,
                    file.getInputStream(),
                    metadata
            );
            s3Client.putObject(putObjectRequest);


            URL s3FullUrl = s3Client.getUrl(bucketName, s3Key);
            user.setFoto(s3FullUrl.toString());
            user.setProfilePictureContentType(file.getContentType());
            userRepository.save(user);

            return s3FullUrl.toString();
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("Falha ao fazer upload da imagem para o S3: " + e.getMessage(), e);
        }
    }

    public URL getProfileImage(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com ID: " + userId));
        String s3Key = user.getFoto();
        if (s3Key == null || s3Key.isEmpty()) {
            throw new RuntimeException("URL da imagem de perfil não encontrada para o usuário ID: " + userId);
        }

        if (s3Key.startsWith("http://") || s3Key.startsWith("https://")) {
            try {
                return new URL(s3Key);
            } catch (Exception e) {
                throw new RuntimeException("URL da foto de perfil inválida para o usuário: " + userId, e);
            }
        } else {
            return s3Client.getUrl(bucketName, s3Key);
        }
    }

    public String getProfileImageContentType(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com ID: " + userId));
        return user.getProfilePictureContentType();
    }
    public Optional<User> getUserEntityById(Long id){
        return userRepository.findById(id);
    }

    private UserDTO convertToDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                null,
                user.getDataCriacao(),
                user.getFcmToken(),
                user.getBio(),
                user.getRole(),
                user.getMusico() != null ? user.getMusico().getNomeArtistico() : null,
                user.getMusico() != null ? user.getMusico().getRedesSociais() : null,
                user.getFoto(),
                user.isProfileCompleted()
        );
    }
}