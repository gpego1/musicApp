package br.com.project.music.business.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Time;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @Column(name = "hora_encerramento", nullable = true)
    private LocalDateTime horaEncerramento;

    @Column(name = "descricao")
    private String descricao;

    @Column(name = "classificacao", nullable = false)
    private String classificacao;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_genero_musical")
    private Genre generoMusical;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_local_evento")
    private Place localEvento;

    @OneToMany(mappedBy = "evento")
    @JsonIgnore
    private List<Reserva> reservas;


    @OneToMany(mappedBy = "idEscala.evento", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Escala> escalasDoEvento = new ArrayList<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_usuario_host", nullable = false)
    @JsonBackReference
    private User host;

    @Column(name = "foto", nullable = true)
    private String foto;

    @Column(name = "foto_content_type", nullable = true)
    private String eventPictureContentType;

    public boolean isContractTimeWithinEvent(LocalDateTime contractStartTime, LocalDateTime contractEndTime) {
        if (contractStartTime == null || contractEndTime == null || dataHora == null || horaEncerramento == null) {
            return false;
        }
        return !contractStartTime.isBefore(this.dataHora) && !contractEndTime.isAfter(this.horaEncerramento);
    }

}
