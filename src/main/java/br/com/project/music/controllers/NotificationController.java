package br.com.project.music.controllers;

import br.com.project.music.business.dtos.NotificationRequest;
import br.com.project.music.business.entities.Notification;

import br.com.project.music.business.entities.User;
import br.com.project.music.business.repositories.NotificationRepository;
import br.com.project.music.business.repositories.UserRepository;
import br.com.project.music.services.NotificationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/notifications")
@Tag(name="Notificações", description = "Gerenciamento de notificações")

public class NotificationController {

    @Autowired
    private NotificationService notificationService;

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

    @PostMapping
    public ResponseEntity<Notification> createNotification(@RequestBody NotificationRequest request) {
        try {
            Notification notification = notificationService.createNotification(request.getUsuarioId(), request.getMensagem());
            return new ResponseEntity<>(notification, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
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
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Notification> markNotificationAsRead(@PathVariable Long id) {
        try {
            Notification updatedNotification = notificationService.markNotificationAsRead(id);
            return new ResponseEntity<>(updatedNotification, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
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