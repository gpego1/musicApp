package br.com.project.music.business.dtos;

import br.com.project.music.business.entities.Event;
import br.com.project.music.business.entities.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservaDTO {
    private Long idReserva;
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User usuario;
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Event evento;
    private boolean confirmado;
}
