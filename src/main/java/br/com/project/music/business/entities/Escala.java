package br.com.project.music.business.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Entity
@Table(name = "escala")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Escala {
    @EmbeddedId
    @EqualsAndHashCode.Include
    private EscalaId idEscala;

    @ManyToOne
    @JoinColumn(name = "id_evento", insertable = false, updatable = false)
    @JsonIgnore
    private Event evento;

    @ManyToOne
    @JoinColumn(name = "id_genero_musical", insertable = false, updatable = false)
    @JsonIgnore
    private Genre genero;

    @ManyToOne
    @JoinColumn(name = "id_musico")
    private Musico musico;

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class EscalaId implements Serializable {
        @ManyToOne
        @JoinColumn(name = "id_evento", insertable = false, updatable = false)
        private Event evento;

        @ManyToOne
        @JoinColumn(name = "id_genero_musical", insertable = false, updatable = false)
        private Genre genero;
    }
    public Escala(EscalaId idEscala, Musico musico) {
        this.idEscala = idEscala;
        this.musico = musico;
    }
}
