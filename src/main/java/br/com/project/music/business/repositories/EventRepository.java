package br.com.project.music.business.repositories;

import br.com.project.music.business.entities.Event;
import br.com.project.music.business.entities.Genre;
import br.com.project.music.business.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    Optional<Event> findByIdEvento(Long idEvento);
    @Query("SELECT e FROM Event e JOIN FETCH e.generoMusical JOIN FETCH e.localEvento")
    List<Event> findAllWithRelations();
    List<Event> findByHost(User host);
    List<Event> findByGeneroMusical(Genre generoMusical);

    @Query("SELECT e FROM Event e WHERE DATE(e.dataHora) = DATE(:data)")
    List<Event> findByData(@Param("data") LocalDateTime data);
    List<Event> findByDataHoraAfter(LocalDateTime now);
    List<Event> findByDataHoraBefore(LocalDateTime now);

}
