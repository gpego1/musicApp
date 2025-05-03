package br.com.project.music.business.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "notification")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id_notificacao")
    private Long idNotificacao;

    @ManyToOne
    @JoinColumn(name = "id_usuario")
    @JsonBackReference
    private User usuario;

    @Column(nullable = false)
    private String mensagem;

    @Column(nullable = true)
    private boolean lida = false;
}
