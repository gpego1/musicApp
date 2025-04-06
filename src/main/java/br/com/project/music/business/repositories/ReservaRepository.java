package br.com.project.music.business.repositories;

import br.com.project.music.business.dtos.ReservaDTO;
import br.com.project.music.business.entities.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {
}
