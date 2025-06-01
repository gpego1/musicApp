package br.com.project.music.business.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

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

    @OneToOne
    @JoinColumn(name="id_usuario", nullable = false, unique = true)
    private User usuario;

    @Column(nullable = false, name="nome_artistico")
    private String nomeArtistico;

    @Column(name = "redes_sociais")
    private String redesSociais;


    @ManyToMany(mappedBy = "musicos")
    @JsonIgnore
    private List<Escala> escalas;


}
