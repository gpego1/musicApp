// src/main/java/br/com/project/music/controllers/NotificationController.java
package br.com.project.music.controllers; // Assuming your controllers are in a 'controllers' package

import br.com.project.music.business.dtos.NotificationRequest;
import br.com.project.music.business.entities.User; // Import your User entity
import br.com.project.music.services.AuthService;
import br.com.project.music.services.FirebaseService;
import br.com.project.music.services.UserService;
import com.google.firebase.messaging.FirebaseMessagingException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final FirebaseService firebaseService;
    private final AuthService authService;
    private final UserService userService;

    @Autowired
    public NotificationController(FirebaseService firebaseService, AuthService authService, UserService userService) {
        this.firebaseService = firebaseService;
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/fcm/register")
    public ResponseEntity<String> registerFcmToken(@RequestBody Map<String, String> requestBody, HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorization token missing or invalid.");
        }

        String authToken = authorizationHeader.substring(7);
        String userEmail = authService.getEmailFromToken(authToken);

        if (userEmail == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired authentication token.");
        }

        User user = userService.getUserByEmail(userEmail).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found for the provided token.");
        }

        String fcmToken = requestBody.get("fcmToken");
        if (fcmToken == null || fcmToken.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("FCM Token is required in the request body.");
        }

        try {
            firebaseService.storeUserToken(user.getId(), fcmToken);
            return ResponseEntity.ok("FCM Token registered successfully for user: " + user.getEmail());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error registering FCM token: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error: " + e.getMessage());
        }
    }

    @PostMapping("/fcm/send")
    public ResponseEntity<String> sendNotification(@RequestBody NotificationRequest request) {
        try {
            String messageId = firebaseService.sendNotificationToUser(request);
            return ResponseEntity.ok("Notificação enviada com sucesso! Message ID: " + messageId);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Erro na requisição: " + e.getMessage());
        } catch (FirebaseMessagingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao enviar notificação via Firebase: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno do servidor: " + e.getMessage());
        }
    }

    @GetMapping("/auth/user/me")
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
}