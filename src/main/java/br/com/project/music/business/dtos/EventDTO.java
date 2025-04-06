package br.com.project.music.business.dtos;

import br.com.project.music.business.entities.Genre;
import br.com.project.music.business.entities.Place;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventDTO {
    private Long idEvento;
    private String nomeEvento;
    private LocalDateTime dataHora;
    private String descricao;
    private Genre generoMusical;
    private Place localEvento;
}
