package br.com.project.music.business.dtos;

import br.com.project.music.business.entities.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MusicoDTO {
    private Long idMusico;
    private User usuario;
    private String nomeArtistico;
    private String redesSociais;
}
