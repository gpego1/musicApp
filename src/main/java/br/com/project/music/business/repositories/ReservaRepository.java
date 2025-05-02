package br.com.project.music.business.repositories;

import br.com.project.music.business.dtos.ReservaDTO;
import br.com.project.music.business.entities.Event;
import br.com.project.music.business.entities.Reserva;
import br.com.project.music.business.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    List<Reserva> findByUsuario(User usuario);
    List<Reserva> findByEvento(Event evento);
    List<Reserva> findByUsuarioAndEvento(User usuario, Event evento);
}
