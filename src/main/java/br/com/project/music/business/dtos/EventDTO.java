package br.com.project.music.business.dtos;

import br.com.project.music.business.entities.Genre;
import br.com.project.music.business.entities.Place;
import br.com.project.music.business.entities.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class EventDTO {
    private Long idEvento;
    private String nomeEvento;
    private LocalDateTime dataHora;
    private String descricao;
    private Genre generoMusical;
    private Place localEvento;
    private User host;
    private String foto;
    private String eventPictureContentType;
}
