package br.com.project.music.business.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "musico")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Musico {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_musico")
    private Long idMusico;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="id_usuario")
    private User usuario;

    @Column(nullable = false, name="nome_artistico")
    private String nomeArtistico;

    @Column(name = "redes_sociais")
    private String redesSociais;

}
