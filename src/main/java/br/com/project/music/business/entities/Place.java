package br.com.project.music.business.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="local_evento")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Place {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_local_evento")
    private Long idLocalEvento;

    @Column(name = "nome_local")
    private String local;
}
