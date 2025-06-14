package br.com.project.music.business.dtos;
import br.com.project.music.business.entities.User;
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
    private String fcmToken;
    private String bio;
    private User.Role role;
    private String nomeArtistico;
    private String redesSociais;
    private String foto;
    private Boolean profileCompleted;

}
