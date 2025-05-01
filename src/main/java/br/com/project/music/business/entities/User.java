package br.com.project.music.business.entities;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Entity
@Table(name="usuario")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String name;
    @Column(unique = true, nullable = false)
    private String email;
    @Column(nullable = false)
    private String senha;


    @Column(name = "data_criacao", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp dataCriacao;

    private String googleId;
    private String foto;

    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private Musico musico;

    public enum Role{
        USER,
        CLIENT,
        HOST,
        ARTISTA
    }
    @Enumerated
    @Column(nullable = false)
    private Role role;

    public boolean isGoogleUser() {return this.googleId != null;}

    public boolean isArtista(){return Role.ARTISTA.equals(this.role);}
    public boolean isClient() {return Role.CLIENT.equals(this.role);}
    public boolean isHost() {return Role.HOST.equals(this.role);}

    public void setMusico(Musico musico) {
        if (isArtista()) {
            this.musico = musico;
            musico.setUsuario(this);
        } else {
            throw new IllegalStateException("Apenas usuários do tipo ARTISTA podem ter um perfil de músico");
        }
    }
    public void setRole(Role role) {this.role = role;}

}
