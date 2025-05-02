package br.com.project.music.services;

import br.com.project.music.business.dtos.ReservaDTO;
import br.com.project.music.business.entities.Reserva;
import br.com.project.music.business.entities.User;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public interface ReservaService {
    ReservaDTO createReserva(ReservaDTO reservaDTO);
    List<ReservaDTO> getAllReservas();
    Optional<ReservaDTO> getReservaById(Long id);
    ReservaDTO updateReserva(Long id, ReservaDTO reservaDTO);
    void deleteReservaById(Long id);
    List<ReservaDTO> getReservasByUsuario(Long userId);
    List<ReservaDTO> getReservasByEvento(Long eventoId);
    List<ReservaDTO> getReservasByUserAndEvent(Long userId, Long eventoId);
}