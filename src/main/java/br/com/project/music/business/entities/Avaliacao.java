package br.com.project.music.business.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="avaliacao")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Avaliacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id_avaliacao")
    private Long idAvaliacao;

    @Column(nullable = false)
    private int nota;

    @Column
    private String mensagem;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_usuario")
    private User usuario;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_evento")
    private Event evento;

    public void setNota(int nota) {
        if(nota < 1 || nota > 5) {
            throw new IllegalArgumentException("Nota deve ser entre 1 e 5.");
        }
        this.nota = nota;
    }
}
