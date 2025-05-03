package br.com.project.music.services;

import br.com.project.music.business.entities.Notification;
import br.com.project.music.business.entities.User;
import br.com.project.music.business.repositories.NotificationRepository;
import br.com.project.music.business.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Notification> getAllNotifications() {return notificationRepository.findAll();}

    public Notification createNotification(Long usuarioId, String mensagem) {
        Optional<User> userOptional = userRepository.findById(usuarioId);
        if (userOptional.isPresent()) {
            Notification notification = new Notification();
            notification.setUsuario(userOptional.get());
            notification.setMensagem(mensagem);
            return notificationRepository.save(notification);
        } else {
            throw new IllegalArgumentException("Usuário com ID " + usuarioId + " não encontrado.");
        }
    }

    public Optional<Notification> getNotificationById(Long id) {
        return notificationRepository.findById(id);
    }

    public Notification markNotificationAsRead(Long id) {
        Optional<Notification> notificationOptional = notificationRepository.findById(id);
        if (notificationOptional.isPresent()) {
            Notification notification = notificationOptional.get();
            notification.setLida(true);
            return notificationRepository.save(notification);
        } else {
            throw new IllegalArgumentException("Notificação com ID " + id + " não encontrada.");
        }
    }

    public void deleteNotification(Long id) {
        if (notificationRepository.existsById(id)) {
            notificationRepository.deleteById(id);
        } else {
            throw new IllegalArgumentException("Notificação com ID " + id + " não encontrada.");
        }
    }
}