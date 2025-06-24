package br.com.project.music.controllers;
import br.com.project.music.business.dtos.*;
import br.com.project.music.business.entities.User;
import br.com.project.music.business.repositories.UserRepository;
import br.com.project.music.services.AuthService;
import br.com.project.music.services.EmailService;
import br.com.project.music.services.FirebaseService;
import br.com.project.music.services.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autentificacao")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;
    private final UserService userService;
    private final ClientRegistrationRepository clientRegistrationRepository;
    private final OAuth2AuthorizationRequestResolver authorizationRequestResolver;
    private final FirebaseService firebaseService;

    @Autowired
    private EmailService emailService;

    @Autowired
    public AuthController(AuthService authService, UserService userService, ClientRegistrationRepository clientRegistrationRepository, FirebaseService firebaseService) {
        this.authService = authService;
        this.userService = userService;
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.authorizationRequestResolver = new DefaultOAuth2AuthorizationRequestResolver(
                this.clientRegistrationRepository, "/oauth2/authorization");
        this.firebaseService = firebaseService;
    }

    @Autowired
    private UserRepository userRepository;


    @PostMapping("/register")
    public ResponseEntity<UserDTO> registerUser(@Valid @RequestBody UserDTO userDTO) {
        UserDTO createdUser = userService.createUser(userDTO);
        if (createdUser.getFcmToken() != null && createdUser.getId() != null) {
            try {
                firebaseService.storeUserToken(createdUser.getId(), createdUser.getFcmToken());
            } catch (Exception e) {
                logger.warn("Failed to store FCM token for user {}: {}", createdUser.getId(), e.getMessage());
            }
        }
        Auth auth = new Auth();
        auth.setEmail(userDTO.getEmail());
        auth.setSenha(userDTO.getSenha());

        String token = authService.authenticate(auth);
        logger.trace("Token gerado: " + token);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }
    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody Auth auth) {
        String token = authService.authenticate(auth);
        if (token != null) {
            logger.trace("Usuario autenticado: " + auth.getEmail() + "Token: " + token);
            return ResponseEntity.ok(token);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @GetMapping("/check-email")
    public ResponseEntity<String> checkEmailExists(@RequestParam String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            try {
                emailService.resetPasswordEmail(email);
                return ResponseEntity.ok("Email para redefinição de senha enviado com sucesso!");
            } catch (Exception e) {
                System.err.println("Erro ao tentar enviar email de redefinição para " + email + ": " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ocorreu um erro ao enviar o email de redefinição. Tente novamente mais tarde.");
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Email não encontrado.");
        }
    }
    @GetMapping("/validate-email")
    public ResponseEntity<String> validateEmailExists(@RequestParam String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Email não encontrado.");
        }
    }
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequest requestDTO){
        Optional<User> userOptional = userService.getUserByEmail(requestDTO.getEmail());
        if(userOptional.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Email não encontrado.");
        }
        User user = userOptional.get();
        userService.changePassword(user, requestDTO.getNewPassword());
        return ResponseEntity.ok("Sua senha foi redefinida com sucesso!");
    }

    @GetMapping("/oauth2/authorization/google")
    public RedirectView googleLoginRedirect(HttpServletRequest request, @RequestParam Map<String, String> params) {
        OAuth2AuthorizationRequest authorizationRequest = authorizationRequestResolver.resolve(request, "google");
        if (authorizationRequest == null) {
            return new RedirectView("/login-failure");
        }
        return new RedirectView(authorizationRequest.getAuthorizationUri());
    }
    @PostMapping("/google")
    public ResponseEntity<?> loginWithGoogle(@RequestBody Map<String, String> request) {
        try {
            String token = request.get("token");
            if (token == null || token.isEmpty()) {
                return ResponseEntity.badRequest().body(new AuthResponse("Token não fornecido"));
            }
            GoogleUserInfo googleUserInfo = authService.verifyGoogleToken(token);
            User user = userService.registerOrLoginGoogleUser(
                    new DefaultOAuth2User(
                            Collections.emptySet(),
                            Map.of(
                                    "sub", googleUserInfo.getId(),
                                    "email", googleUserInfo.getEmail(),
                                    "name", googleUserInfo.getName(),
                                    "picture", googleUserInfo.getPicture()
                            ),
                            "email"
                    )
            );
            String jwtToken = authService.authenticateWithGoogle(token);

            String finalProfilePictureUrl = null;
            if (user.getGoogleId() != null && user.getGoogleProfilePictureUrlS3() != null && !user.getGoogleProfilePictureUrlS3().isEmpty()) {
                finalProfilePictureUrl = user.getGoogleProfilePictureUrlS3();
                logger.debug("Usando URL da foto de perfil do S3 para usuário Google: {}", finalProfilePictureUrl);
            }
            else if (user.getGoogleId() != null && user.getGoogleProfilePictureUrl() != null && !user.getGoogleProfilePictureUrl().isEmpty()) {
                finalProfilePictureUrl = user.getGoogleProfilePictureUrl();
                logger.debug("Usando URL da foto de perfil original do Google como fallback: {}", finalProfilePictureUrl);
            }
            else if (user.getFoto() != null && !user.getFoto().isEmpty()) {
                finalProfilePictureUrl = "/users/" + user.getId() + "/profile-image";
                logger.debug("Usando URL da foto de perfil local: {}", finalProfilePictureUrl);
            } else {
                logger.debug("Nenhuma URL de foto de perfil encontrada para o usuário.");
            }
            AuthResponse authResponse = new AuthResponse(
                    jwtToken,
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    user.getRole(),
                    finalProfilePictureUrl,
                    user.getGoogleId() != null,
                    user.getSenha() != null
            );
            return ResponseEntity.ok(authResponse);
        } catch (IllegalArgumentException e) {
            logger.error("Dados do Google incompletos ou inválidos: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new AuthResponse("Dados incompletos ou inválidos: " + e.getMessage()));
        } catch (IllegalStateException e) {
            logger.error("Conflito com conta existente: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new AuthResponse("Conflito de conta: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Falha geral no login com Google", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthResponse("Falha na autenticação: " + e.getMessage()));
        }
    }
    @GetMapping("/login-success")
    public ResponseEntity<String> loginSuccess(OAuth2AuthenticationToken oauthToken) {
        if (oauthToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Token OAuth2 não encontrado");
        }
        try {
            User user = userService.registerOrLoginGoogleUser(oauthToken.getPrincipal());
            String token = authService.generateTokenForGoogle(user.getEmail());
            return ResponseEntity.ok(token);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Dados do Google incompletos: " + e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Conta de email já associada a outro usuário: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro durante o login com Google: " + e.getMessage());
        }
    }

    @GetMapping("/login-failure")
    public ResponseEntity<String> loginFailure() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Falha na autenticação com Google");
    }
    @GetMapping("/user/me")
    public ResponseEntity<Map<String, Object>> getUserDetails(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            String userEmail = authService.getEmailFromToken(token);
            if (userEmail != null) {
                User user = userService.getUserByEmail(userEmail).orElse(null);
                if (user != null) {
                    return ResponseEntity.ok(Map.of("id", user.getId(), "nome", user.getName(), "role", user.getRole().name(), "bio", user.getBio()));
                }
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    @PostMapping("/user/me/upload")
    public ResponseEntity<String> uploadProfileImage(@RequestParam("foto") MultipartFile file, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (userDetails == null) {
                return new ResponseEntity<>("Usuário não autorizado.", HttpStatus.UNAUTHORIZED);
            }
            String email = userDetails.getUsername();
            Optional<User> userOptional = userService.getUserByEmail(email);
            if (userOptional.isEmpty()) {
                return new ResponseEntity<>("Usuário não encontrado.", HttpStatus.NOT_FOUND);
            }
            User user = userOptional.get();
            String s3Key = userService.uploadProfileImage(user.getId(), file);
            return new ResponseEntity<>("Foto de perfil upada com sucesso! Chave S3: " + s3Key, HttpStatus.OK);

        } catch (IOException e) {
            System.err.println("Erro de IO ao fazer upload da imagem: " + e.getMessage());
            return new ResponseEntity<>("Erro ao fazer upload da imagem: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (RuntimeException e) {
            System.err.println("Erro ao processar upload da imagem: " + e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/user/me/profile-image")
    public ResponseEntity<Void> getProfileImage(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String email = userDetails.getUsername();
        Optional<User> userOptional = userService.getUserByEmail(email);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        User user = userOptional.get();
        String finalImageUrl = null;

        if (user.getGoogleId() != null && !user.getGoogleId().isEmpty() &&
                user.getGoogleProfilePictureUrlS3() != null && !user.getGoogleProfilePictureUrlS3().isEmpty()) {
            finalImageUrl = user.getGoogleProfilePictureUrlS3();
        }
        else if (user.getGoogleId() != null && !user.getGoogleId().isEmpty() &&
                user.getGoogleProfilePictureUrl() != null && !user.getGoogleProfilePictureUrl().isEmpty()) {
            finalImageUrl = user.getGoogleProfilePictureUrl();
        }
        else if (user.getFoto() != null && !user.getFoto().isEmpty()) {
            finalImageUrl = user.getFoto();
        }

        if (finalImageUrl != null) {
            try {
                return ResponseEntity.status(HttpStatus.FOUND)
                        .location(new URI(finalImageUrl))
                        .build();
            } catch (URISyntaxException e) {
                System.err.println("Erro de URI ao redirecionar para imagem: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }
        try {
            URL s3ImageUrl = userService.getProfileImage(user.getId());
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(s3ImageUrl.toURI())
                    .build();

        } catch (RuntimeException e) {
            if (e.getMessage().contains("Usuário não encontrado") || e.getMessage().contains("Chave S3 da imagem de perfil não encontrada") || e.getMessage().contains("URL da imagem de perfil não encontrada")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            System.err.println("Erro ao buscar imagem de perfil do S3: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (URISyntaxException e) {
            System.err.println("Erro de sintaxe de URI ao redirecionar para imagem S3: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    @GetMapping("/users/{userId}/profile-image")
    public ResponseEntity<Void> getUserProfileImageById(@PathVariable Long userId) {
        Optional<User> userOptional = userService.getUserEntityById(userId);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        User user = userOptional.get();
        String finalImageUrl = null;

        if (user.getGoogleId() != null && !user.getGoogleId().isEmpty() &&
                user.getGoogleProfilePictureUrlS3() != null && !user.getGoogleProfilePictureUrlS3().isEmpty()) {

            finalImageUrl = user.getGoogleProfilePictureUrlS3();
            System.out.println("DEBUG: Usando URL do S3 para foto de perfil do usuário " + userId + ": " + finalImageUrl);

        }
        else if (user.getGoogleId() != null && !user.getGoogleId().isEmpty() &&
                user.getGoogleProfilePictureUrl() != null && !user.getGoogleProfilePictureUrl().isEmpty()) {

            finalImageUrl = user.getGoogleProfilePictureUrl();
            System.out.println("DEBUG: Usando URL original do Google como fallback para usuário " + userId + ": " + finalImageUrl);
        }
        else if (user.getFoto() != null && !user.getFoto().isEmpty()) {
            finalImageUrl = user.getFoto();
            System.out.println("DEBUG: Usando URL do campo 'foto' para usuário " + userId + ": " + finalImageUrl);
        }

        if (finalImageUrl != null) {
            try {
                return ResponseEntity.status(HttpStatus.FOUND)
                        .location(new URI(finalImageUrl))
                        .build();
            } catch (URISyntaxException e) {
                System.err.println("Erro de URI ao redirecionar para imagem do usuário " + userId + ": " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }
        try {
            URL s3ImageUrl = userService.getProfileImage(userId);
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(s3ImageUrl.toURI())
                    .build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Usuário não encontrado") || e.getMessage().contains("Chave S3 da imagem de perfil não encontrada") || e.getMessage().contains("Arquivo S3 não encontrado")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            System.err.println("Erro ao buscar imagem de perfil do S3 para usuário " + userId + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (URISyntaxException e) {
            System.err.println("Erro de sintaxe de URI ao redirecionar para imagem S3 para usuário " + userId + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(HttpServletRequest request, @Valid @RequestBody ChangePasswordDTO changePasswordDTO) {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            String userEmail = authService.getEmailFromToken(token);
            if (userEmail != null) {
                User user = userService.getUserByEmail(userEmail).orElse(null);
                if (user != null) {
                    if (userService.checkPassword(user, changePasswordDTO.getCurrentPassword())) {
                        userService.changePassword(user, changePasswordDTO.getNewPassword());
                        return ResponseEntity.ok("Senha alterada com sucesso.");
                    } else {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Senha atual incorreta.");
                    }
                } else {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado.");
                }
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token inválido.");
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token de autenticação não encontrado.");
        }
    }
}