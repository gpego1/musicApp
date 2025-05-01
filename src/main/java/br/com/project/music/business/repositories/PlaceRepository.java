package br.com.project.music.business.repositories;

import br.com.project.music.business.entities.Place;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlaceRepository extends JpaRepository<Place, Long> {
    Optional<Place> findByLocal(String local);
}
