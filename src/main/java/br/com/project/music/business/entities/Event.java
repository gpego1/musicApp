package br.com.project.music.business.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name="evento")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evento")
    private Long idEvento;

    @Column(name = "nome_evento", nullable = false)
    private String nomeEvento;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Column(name = "descricao")
    private String descricao;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_genero_musical")
    private Genre generoMusical;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_local_evento")
    private Place localEvento;

}
