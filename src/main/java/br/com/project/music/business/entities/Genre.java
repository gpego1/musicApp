package br.com.project.music.business.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "genero_musical")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Genre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_genero_musical")
    private Long idGeneroMusical;

    @Column(name="nomeGenero", nullable = false)
    private String nomeGenero;

    @OneToMany(mappedBy = "idEscala.genero", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Escala> escalasDoGenero = new ArrayList<>();
}
