package br.com.project.music.business.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "escala")
@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Escala {
    @EmbeddedId
    @EqualsAndHashCode.Include
    private EscalaId idEscala;

    @ManyToMany
    @JoinTable(
            name = "escala_musico",
            joinColumns = {
                    @JoinColumn(name = "id_evento", referencedColumnName = "id_evento"),
                    @JoinColumn(name = "id_genero_musical", referencedColumnName = "id_genero_musical")
            },
            inverseJoinColumns = @JoinColumn(name = "id_musico", referencedColumnName = "id_musico")
    )
    @JsonManagedReference
    private List<Musico> musicos = new ArrayList<>();

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class EscalaId implements Serializable {
        @ManyToOne
        @JoinColumn(name = "id_evento")
        private Event evento;

        @ManyToOne
        @JoinColumn(name = "id_genero_musical")
        private Genre genero;
    }
    public Escala(EscalaId idEscala, List<Musico> musicos) {
        this.idEscala = idEscala;
        this.musicos = musicos;
    }
}
