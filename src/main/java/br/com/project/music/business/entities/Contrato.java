package br.com.project.music.business.entities;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
@Entity
@Table(name="contrato")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Contrato {
    @EmbeddedId
    @EqualsAndHashCode.Include
    private ContratoId idContrato;

    @Column(name = "valor")
    private Double valor;

    @Column(name = "detalhes")
    private String detalhes;

    @ManyToOne
    @JoinColumn(name = "id_evento", insertable = false, updatable = false)
    @JsonIgnore
    private Event evento;

    @ManyToOne
    @JoinColumn(name = "id_musico", insertable = false, updatable = false)
    @JsonIgnore
    private Musico musico;

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class ContratoId implements Serializable{
        @ManyToOne
        @JoinColumn(name = "id_evento")
        private Event evento;

        @ManyToOne
        @JoinColumn(name = "id_musico")
        private Musico musico;

    }
    public Contrato(ContratoId idContrato, Double valor, String detalhes) {
        this.idContrato = idContrato;
        this.valor = valor;
        this.detalhes = detalhes;
    }
}






