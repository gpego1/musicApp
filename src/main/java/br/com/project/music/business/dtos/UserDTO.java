package br.com.project.music.business.dtos;
import lombok.*;
import br.com.project.music.business.entities.User.Role;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String name;
    private String email;
    private String senha;
    private Timestamp dataCriacao;
    private Role role;
    //Se for musico:
    private String nomeArtistico;
    private String redesSociais;
}
