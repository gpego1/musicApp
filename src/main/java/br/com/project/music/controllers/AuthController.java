package br.com.project.music.controllers;

import br.com.project.music.business.dtos.Auth;
import br.com.project.music.business.dtos.UserDTO;
import br.com.project.music.services.AuthService;
import br.com.project.music.services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @Autowired
    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
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
        return ResponseEntity.badRequest().build();
    }
    @GetMapping("/login-success")
    public ResponseEntity<String> loginSuccess(OAuth2AuthenticationToken oauthToken) {
        try {
            var user = userService.registerOrLoginGoogleUser(oauthToken.getPrincipal());

            Auth auth = new Auth();
            auth.setEmail(user.getEmail());
            auth.setSenha("");

            String token = authService.authenticate(auth);
            return ResponseEntity.ok(token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Falha no login com Google: " + e.getMessage());
        }
    }
    @GetMapping("/login-failure")
    public ResponseEntity<String> loginFailure() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Falha na autenticação com Google");
    }
}