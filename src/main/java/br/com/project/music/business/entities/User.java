package br.com.project.music.business.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@Entity
@Table(name="usuario")
@Getter
@Setter
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
    @Column(nullable = false)
    private String userType = "USER";

    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private Musico musico;

    public boolean isArtista() {
        return "ARTISTA".equals(userType);
    }
    public void setMusico(Musico musico) {
        if (isArtista()) {
            this.musico = musico;
            musico.setUsuario(this);
        } else {
            throw new IllegalStateException("Apenas usuários do tipo ARTISTA podem ter um perfil de músico");
        }
    }
}
