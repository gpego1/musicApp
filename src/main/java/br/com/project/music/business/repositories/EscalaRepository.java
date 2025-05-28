package br.com.project.music.business.repositories;

import br.com.project.music.business.entities.Escala;
import br.com.project.music.business.entities.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EscalaRepository extends JpaRepository<Escala, Escala.EscalaId> {
    Escala findEscalaByIdEscala(Escala.EscalaId idEscala);
    List<Escala> findByIdEscala_Evento(Event evento);
    List<Escala> findByIdEscala_Evento_IdEvento(Long idEvento);
}
