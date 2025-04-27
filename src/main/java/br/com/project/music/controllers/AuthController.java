package br.com.project.music.controllers;

import br.com.project.music.business.dtos.Auth;
import br.com.project.music.business.dtos.ChangePasswordDTO;
import br.com.project.music.business.dtos.UserDTO;
import br.com.project.music.business.entities.User;
import br.com.project.music.services.AuthService;
import br.com.project.music.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

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
    public ResponseEntity<Map<String, String>> getUserDetails(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            String userEmail = authService.getEmailFromToken(token);
            if (userEmail != null) {
                User user = userService.getUserByEmail(userEmail).orElse(null);
                if (user != null) {
                    return ResponseEntity.ok(Map.of("nome", user.getName()));
                }
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
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