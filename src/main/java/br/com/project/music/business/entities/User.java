package br.com.project.music.business.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;
import java.util.List;

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
    @Column(nullable = true)
    private String senha;


    @Column(name = "data_criacao", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp dataCriacao;

    @Column(name = "biografia", nullable = true)
    private String bio;

    @Column(name = "google_id", nullable = true)
    private String googleId;

    @Column(name = "google_pfp", length = 500, nullable = true)
    private String googleProfilePictureUrl;

    @Column(name = "foto", nullable = true)
    private String foto;

    @Column(name = "foto_content_type", nullable = true)
    private String profilePictureContentType;

    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Musico musico;

    @OneToMany(mappedBy = "usuario")
    @JsonManagedReference
    @JsonIgnore
    private List<Notification> notifications;

    @OneToMany(mappedBy = "usuario")
    @JsonIgnore
    private List<Reserva> reservas;

    @OneToMany(mappedBy = "host")
    @JsonIgnore
    private List<Event> eventos;

    @Column(nullable = false)
    private boolean profileCompleted = false;

    public boolean isProfileCompleted() {
        return profileCompleted;
    }

    public enum Role{
        CLIENT,
        HOST,
        ARTISTA
    }
    @Enumerated
    @Column(nullable = false)
    private Role role;

    public boolean isGoogleUser() {return this.googleId != null;}

    public boolean isArtista(){return Role.ARTISTA.equals(this.role);}
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

    public User(String email, Role role, String googleId) {
        this.email = email;
        this.role = role;
        this.googleId = googleId;
        this.profileCompleted = false;
    }
}
