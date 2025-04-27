package br.com.project.music.business.entities;

import jakarta.persistence.*;
import lombok.*;

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

    @Column(name="nome_genero", nullable = false)
    private String nomeGenero;
}
