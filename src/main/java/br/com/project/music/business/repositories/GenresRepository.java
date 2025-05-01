package br.com.project.music.business.repositories;

import br.com.project.music.business.entities.Genre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GenresRepository extends JpaRepository<Genre, Long> {
    Optional<Genre> findByNomeGenero(String nomeGenero);
}
