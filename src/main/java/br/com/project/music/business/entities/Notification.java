package br.com.project.music.business.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id_notificacao")
    private Long idNotificacao;

    @ManyToOne
    @JoinColumn(name = "id_usuario")
    private User usuario;

    @Column(nullable = false)
    private String mensagem;

    @Column(nullable = false)
    private boolean lida = false;
}
