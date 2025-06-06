package br.com.project.music.business.dtos;

import br.com.project.music.business.entities.Genre;
import br.com.project.music.business.entities.Place;
import br.com.project.music.business.entities.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Time;
import java.time.LocalDateTime;
import java.time.LocalTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class EventDTO {
    private Long idEvento;
    private String nomeEvento;
    private LocalDateTime dataHora;
    private LocalTime horaEncerramento;
    private String descricao;
    private String classificacao;
    private Genre generoMusical;
    private Place localEvento;
    private User host;
    private String foto;
    private String eventPictureContentType;
}
