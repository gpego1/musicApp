package br.com.project.music.business.dtos;

import br.com.project.music.business.entities.Event;
import br.com.project.music.business.entities.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservaDTO {
    private Long idReserva;
    private User usuario;
    private Event evento;
    private boolean confirmado;
}
