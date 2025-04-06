package br.com.project.music.business.dtos;

import br.com.project.music.business.entities.Event;
import br.com.project.music.business.entities.User;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class ReservaDTO {
    private Long idReserva;
    private User usuario;
    private Event evento;
    private boolean confirmado;
}
