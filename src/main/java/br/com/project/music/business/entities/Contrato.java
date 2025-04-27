package br.com.project.music.business.entities;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
@Entity
@Table(name="contrato")
@IdClass(ContratoId.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Contrato {
    @Id
    @Column(name = "id_musico")
    private Long idMusico;

    @Id
    @Column(name = "id_reserva")
    private Long idReserva;

    @ManyToOne
    @JoinColumn(name = "id_musico", insertable = false, updatable = false)
    private Musico musico;

    @ManyToOne
    @JoinColumn(name = "id_reserva", insertable = false, updatable = false)
    private Reserva reserva;
}

    class ContratoId implements Serializable {

        private Long idMusico;
        private Long idReserva;

        public ContratoId() {
        }

        public ContratoId(Long idMusico, Long idReserva) {
            this.idMusico = idMusico;
            this.idReserva = idReserva;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ContratoId that = (ContratoId) o;

            if (idMusico != null ? !idMusico.equals(that.idMusico) : that.idMusico != null) return false;
            return idReserva != null ? idReserva.equals(that.idReserva) : that.idReserva == null;
        }

        @Override
        public int hashCode() {
            int result = idMusico != null ? idMusico.hashCode() : 0;
            result = 31 * result + (idReserva != null ? idReserva.hashCode() : 0);
            return result;
        }



}
