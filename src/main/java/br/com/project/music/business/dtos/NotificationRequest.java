package br.com.project.music.business.dtos;

import lombok.Data;

@Data
public class NotificationRequest {
    private Long usuarioId;
    private String mensagem;
}
