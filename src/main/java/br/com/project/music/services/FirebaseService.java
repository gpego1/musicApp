package br.com.project.music.services;
import br.com.project.music.business.dtos.NotificationRequest;
import br.com.project.music.business.entities.User;
import br.com.project.music.business.repositories.UserRepository;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class FirebaseService {

    private final FirebaseApp firebaseApp;
    private final UserRepository userRepository;

    @Autowired
    public FirebaseService(FirebaseApp firebaseApp, UserRepository userRepository) {
        this.firebaseApp = firebaseApp;
        this.userRepository = userRepository;
        System.out.println("FirebaseService inicializado com FirebaseApp: " + firebaseApp.getName());
    }


    public void storeUserToken(Long userId, String fcmToken) {
        if (userId == null || fcmToken == null || fcmToken.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID and FCM Token must not be null or empty.");
        }

        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setFcmToken(fcmToken);
            userRepository.save(user);
            System.out.println("FCM Token stored successfully for user ID: " + userId);
        } else {
            throw new IllegalArgumentException("User with ID " + userId + " not found.");
        }
    }

    public String sendNotificationToUser(NotificationRequest request) throws FirebaseMessagingException {
        if (request.getUsuarioId() == null) {
            throw new IllegalArgumentException("User ID (usuarioId) is required to send a notification.");
        }

        Optional<User> userOptional = userRepository.findById(request.getUsuarioId());
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("User with ID " + request.getUsuarioId() + " not found.");
        }

        User user = userOptional.get();
        String fcmToken = user.getFcmToken();

        if (fcmToken == null || fcmToken.trim().isEmpty()) {
            throw new IllegalArgumentException("FCM Token not found for user ID: " + request.getUsuarioId() + ". Please ensure it's registered.");
        }

        Notification.Builder notificationBuilder = Notification.builder()
                .setTitle(request.getTitle() != null ? request.getTitle() : "Nova Notificação")
                .setBody(request.getMensagem());

        if (request.getImageUrl() != null && !request.getImageUrl().isEmpty()) {
            notificationBuilder.setImage(request.getImageUrl());
        }

        Message.Builder messageBuilder = Message.builder()
                .setToken(fcmToken)
                .setNotification(notificationBuilder.build());

        if (request.getData() != null && !request.getData().isEmpty()) {
            messageBuilder.putAllData(request.getData());
        }

        Message message = messageBuilder.build();

        try {
            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println("Notification sent successfully to user " + request.getUsuarioId() + " (FCM Token: " + fcmToken + "): " + response);
            return response;
        } catch (FirebaseMessagingException e) {
            System.err.println("Error sending notification to user " + request.getUsuarioId() + " (FCM Token: " + fcmToken + "): " + e.getMessage());
            throw e;
        }
    }
}