package br.com.project.music.business.dtos;

import br.com.project.music.business.entities.User;
import lombok.Data;

@Data
public class MusicoDTO {
    private Long idMusico;
    private User usuario;
    private String nomeArtistico;
    private String redesSociais;
}
