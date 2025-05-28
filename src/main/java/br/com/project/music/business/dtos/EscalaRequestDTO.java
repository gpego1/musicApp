package br.com.project.music.business.dtos;

import lombok.Data;
import java.util.List;

@Data
public class EscalaRequestDTO {
    private Long idEvento;
    private Long idGeneroMusical;
    private List<Long> idsMusicos;
}