package br.com.project.music.business.repositories;

import br.com.project.music.business.entities.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    Optional<Event> findByIdEvento(Long idEvento);
}
