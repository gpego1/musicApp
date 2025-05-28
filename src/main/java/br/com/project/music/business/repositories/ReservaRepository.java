package br.com.project.music.business.repositories;

import br.com.project.music.business.dtos.ReservaDTO;
import br.com.project.music.business.entities.Event;
import br.com.project.music.business.entities.Reserva;
import br.com.project.music.business.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    List<Reserva> findByUsuario(User usuario);
    List<Reserva> findByEvento(Event evento);
    List<Reserva> findByUsuarioIdAndConfirmadoTrue(Long userId);

    @Query("SELECT r FROM Reserva r JOIN r.evento e WHERE r.usuario.id = :userId AND e.dataHora < :now AND r.confirmado = true")
    List<Reserva> findByUsuarioIdAndEventoDataHoraBeforeAndConfirmadoTrue(@Param("userId") Long usuarioId, @Param("now")LocalDateTime now);
}
