package br.com.project.music.controllers;

import br.com.project.music.business.dtos.NotificationRequest;
import br.com.project.music.business.entities.Notification;
import br.com.project.music.business.entities.User; // Import your User entity
import br.com.project.music.business.repositories.NotificationRepository;
import br.com.project.music.business.repositories.UserRepository;
import br.com.project.music.services.AuthService;
import br.com.project.music.services.FirebaseService;
import br.com.project.music.services.NotificationService;
import br.com.project.music.services.UserService;
import com.google.firebase.messaging.FirebaseMessagingException;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/notifications")
@Tag(name="Notificações", description = "Gerenciamento de notificações")
public class NotificationController {

    private final NotificationService notificationService;
    private final FirebaseService firebaseService;
    private final AuthService authService;
    private final UserService userService;

    @Autowired
    public NotificationController(NotificationService notificationService,FirebaseService firebaseService, AuthService authService, UserService userService) {
        this.notificationService = notificationService;
        this.firebaseService = firebaseService;
        this.authService = authService;
        this.userService = userService;
    }

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<Notification>> getAllNotifications() {
        List<Notification> notifications = notificationService.getAllNotifications();
        return new ResponseEntity<>(notifications, HttpStatus.OK);
    }
    @GetMapping("/{id}")
    public ResponseEntity<Notification> getNotificationById(@PathVariable Long id) {
        Optional<Notification> notification = notificationService.getNotificationById(id);
        return notification.map(response -> new ResponseEntity<>(response, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    @GetMapping("user/{userId}")
    public ResponseEntity<List<Notification>> getNotificationsByUserId(@PathVariable Long userId){
        List<Notification> notifications = notificationService.getNotificationsByUserId(userId);
        return new ResponseEntity<>(notifications, HttpStatus.OK);
    }
    @PostMapping("/broadcast")
    public ResponseEntity<String> sendBroadcastNotification(@RequestBody NotificationRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        System.out.println("Usuário " + username + " (com roles: " + authentication.getAuthorities().stream().map(g -> g.getAuthority()).collect(Collectors.joining(", ")) + ") está tentando enviar uma notificação de broadcast.");
        if (request.getMensagem() == null || request.getMensagem().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("A mensagem da notificação é obrigatória.");
        }
        try {
            List<User> allUsers = userRepository.findAll();
            if (allUsers.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Nenhum usuário encontrado para enviar notificação.");
            }
            for (User user : allUsers) {
                Notification notification = new Notification();
                notification.setUsuario(user);
                notification.setMensagem(request.getMensagem());
                notificationRepository.save(notification);
            }
            return ResponseEntity.ok("Notificação enviada com sucesso para " + allUsers.size() + " usuários.");
        } catch (Exception e) {
            System.err.println("Erro ao processar notificação de broadcast: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno ao enviar notificação.");
        }
    }
    @PostMapping("/user/{idUsuario}")
    public ResponseEntity<Notification> sendNotificationToUser(
            @PathVariable Long idUsuario,
            @RequestBody NotificationRequest request) {
        try {
            Notification notification = notificationService.createNotification(idUsuario, request.getMensagem());
            return new ResponseEntity<>(notification, HttpStatus.CREATED);
        }catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
    @PutMapping("/{id}/read")
    public ResponseEntity<Notification> markNotificationAsRead(@PathVariable Long id) {
        try {
            Notification updatedNotification = notificationService.markNotificationAsRead(id);
            return new ResponseEntity<>(updatedNotification, HttpStatus.OK);
        }catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
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
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        try {
            notificationService.deleteNotification(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}