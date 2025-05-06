package br.com.project.music.business.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "reserva")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reserva")
    private Long idReserva;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_usuario")
    private User usuario;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_evento")
    private Event evento;

    @Column(nullable = false)
    private boolean  confirmado = false;

    public boolean isGoogleUser() {return this.usuario != null && this.usuario.isGoogleUser();}
    public String getGoogleId() {return this.usuario != null ? this.usuario.getGoogleId() : null;}

}
