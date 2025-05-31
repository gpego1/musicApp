package br.com.project.music.business.repositories;

import br.com.project.music.business.entities.Escala;
import br.com.project.music.business.entities.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EscalaRepository extends JpaRepository<Escala, Escala.EscalaId> {
    Escala findEscalaByIdEscala(Escala.EscalaId idEscala);
    List<Escala> findByIdEscala_Evento(Event evento);
    List<Escala> findByIdEscala_Evento_IdEvento(Long idEvento);

    @Query("SELECT e FROM Escala e LEFT JOIN FETCH e.musicos")
    List<Escala> findAllWithMusicos();

    @Query("SELECT e FROM Escala e LEFT JOIN FETCH e.musicos WHERE e.idEscala = :id")
    Optional<Escala> findByIdWithMusicos(@Param("id") Escala.EscalaId id);

    @Query("SELECT e FROM Escala e LEFT JOIN FETCH e.musicos WHERE e.idEscala.evento.idEvento = :eventId")
    List<Escala> findEscalasByEventIdWithMusicos(@Param("eventId") Long eventId);
}
