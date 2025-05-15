package br.com.project.music.controllers;

import br.com.project.music.business.dtos.Auth;
import br.com.project.music.business.dtos.ChangePasswordDTO;
import br.com.project.music.business.dtos.GoogleUserInfo;
import br.com.project.music.business.dtos.UserDTO;
import br.com.project.music.business.entities.User;
import br.com.project.music.business.repositories.UserRepository;
import br.com.project.music.services.AuthService;
import br.com.project.music.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;


import java.io.IOException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;
    private final UserService userService;
    private final ClientRegistrationRepository clientRegistrationRepository;
    private final OAuth2AuthorizationRequestResolver authorizationRequestResolver;

    @Autowired
    public AuthController(AuthService authService, UserService userService, ClientRegistrationRepository clientRegistrationRepository) {
        this.authService = authService;
        this.userService = userService;
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.authorizationRequestResolver = new DefaultOAuth2AuthorizationRequestResolver(
                this.clientRegistrationRepository, "/oauth2/authorization");
    }

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<UserDTO> registerUser(@Valid @RequestBody UserDTO userDTO) {
        UserDTO createdUser = userService.createUser(userDTO);
        Auth auth = new Auth();
        auth.setEmail(userDTO.getEmail());
        auth.setSenha(userDTO.getSenha());
        String token = authService.authenticate(auth);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody Auth auth) {
        String token = authService.authenticate(auth);
        if (token != null) {
            return ResponseEntity.ok(token);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
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
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Token não fornecido",
                        "message", "O token do Google é obrigatório"
                ));
            }
            GoogleUserInfo googleUserInfo = authService.verifyGoogleToken(token);
            User user = registerOrUpdateGoogleUser(googleUserInfo);
            String jwtToken = authService.authenticateWithGoogle(token);
            return ResponseEntity.ok(createAuthResponse(jwtToken, user));
        } catch (IllegalArgumentException e) {
            logger.error("Dados do Google incompletos: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Dados incompletos",
                    "message", e.getMessage()
            ));
        } catch (IllegalStateException e) {
            logger.error("Conflito com conta existente: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "error", "Conflito de conta",
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            logger.error("Falha no login com Google", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "error", "Falha na autenticação",
                    "message", e.getMessage()
            ));
        }
    }
    private User registerOrUpdateGoogleUser(GoogleUserInfo googleUserInfo) {
        Map<String, Object> oauthAttributes = Map.of(
                "sub", googleUserInfo.getId(),
                "email", googleUserInfo.getEmail(),
                "name", googleUserInfo.getName(),
                "picture", googleUserInfo.getPicture()
        );
        OAuth2User oauthUser = new DefaultOAuth2User(
                Collections.emptySet(),
                oauthAttributes,
                "email"
        );
        User user = userService.registerOrLoginGoogleUser(oauthUser);
        if(user.getGoogleId() == null) {
            user.setGoogleId(googleUserInfo.getId());
            user = userRepository.save(user);
        }
        return user;
    }

    private Map<String, Object> createAuthResponse(String token, User user) {
        return Map.of(
                "token", token,
                "user", Map.of(
                        "id", user.getId(),
                        "name", user.getName(),
                        "email", user.getEmail(),
                        "role", user.getRole().name(),
                        "photo", user.getFoto(),
                        "isGoogleUser", user.getGoogleId() != null,
                        "hasPassword", user.getSenha() != null

                )
        );
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
                    return ResponseEntity.ok(Map.of("id", user.getId(), "nome", user.getName(), "role", user.getRole().name()));
                }
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    @PostMapping("/user/me/upload")
    public ResponseEntity<String> uploadProfileImage(@RequestParam("foto") MultipartFile file, @AuthenticationPrincipal UserDetails userDetails) {
        try{
            if(userDetails == null){
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String email = userDetails.getUsername();
            Optional<User> userOptional = userService.getUserByEmail(email);

            if(userOptional.isPresent()){
                return new ResponseEntity<>("Usuário não encontrado.", HttpStatus.NOT_FOUND);
            }
            User user = userOptional.get();
            String foto = userService.uploadProfileImage(user.getId(), file);
            return new ResponseEntity<>("Foto de perfil upada com sucesso!: " + foto, HttpStatus.OK);

        } catch (IOException e) {
            return new ResponseEntity<>("Erro ao fazer upload da imagem: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
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