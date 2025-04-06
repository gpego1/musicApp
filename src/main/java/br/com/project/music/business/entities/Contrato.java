package br.com.project.music.business.entities;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.io.Serializable;
@Entity
@Table(name="contrato")
@IdClass(ContratoId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Contrato {
    @Id
    @Column(name = "id_musico")
    private int idMusico;

    @Id
    @Column(name = "id_reserva")
    private int idReserva;

    @ManyToOne
    @JoinColumn(name = "id_musico", insertable = false, updatable = false)
    private Musico musico;

    @ManyToOne
    @JoinColumn(name = "id_reserva", insertable = false, updatable = false)
    private Reserva reserva;
}

    class ContratoId implements Serializable {

        private int idMusico;
        private int idReserva;


        public ContratoId() {
        }

        public ContratoId(int idMusico, int idReserva) {
            this.idMusico = idMusico;
            this.idReserva = idReserva;
        }

        public int getIdMusico() {
            return idMusico;
        }

        public void setIdMusico(int idMusico) {
            this.idMusico = idMusico;
        }

        public int getIdReserva() {
            return idReserva;
        }

        public void setIdReserva(int idReserva) {
            this.idReserva = idReserva;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ContratoId that = (ContratoId) o;

            if (idMusico != that.idMusico) return false;
            return idReserva == that.idReserva;
        }

        @Override
        public int hashCode() {
            int result = idMusico;
            result = 31 * result + idReserva;
            return result;
        }

}
