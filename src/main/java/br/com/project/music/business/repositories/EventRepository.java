package br.com.project.music.business.repositories;

import br.com.project.music.business.entities.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    Optional<Event> findByIdEvento(Long idEvento);

    @Query("SELECT e FROM Event e JOIN FETCH e.generoMusical JOIN FETCH e.localEvento")
    List<Event> findAllWithRelations();
}
