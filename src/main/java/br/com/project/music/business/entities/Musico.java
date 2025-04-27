package br.com.project.music.business.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "musico")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Musico {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_musico")
    private Long idMusico;

    @OneToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name="id_usuario")
    private User usuario;

    @Column(nullable = false, name="nome_artistico")
    private String nomeArtistico;

    @Column(name = "redes_sociais")
    private String redesSociais;



}
