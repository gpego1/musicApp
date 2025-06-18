package br.com.project.music.business.dtos;

import lombok.Data;

import java.util.Map;

@Data
public class NotificationRequest {
    private Long usuarioId;
    private String fcmToken;
    private String title;
    private String mensagem;
    private String imageUrl;
    private Map<String, String> data;
}
