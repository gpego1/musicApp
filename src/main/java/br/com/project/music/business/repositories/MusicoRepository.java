package br.com.project.music.business.repositories;

import br.com.project.music.business.entities.Musico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MusicoRepository extends JpaRepository<Musico, Long> {
    Optional<Musico> findByIdMusico(Long id);
}
