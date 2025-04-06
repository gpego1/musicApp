package br.com.project.music.services.impl;
import br.com.project.music.business.dtos.ReservaDTO;
import br.com.project.music.business.entities.Reserva;
import br.com.project.music.business.repositories.ReservaRepository;
import br.com.project.music.services.ReservaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReservaServiceImpl implements ReservaService {

    @Autowired
    private ReservaRepository reservaRepository;

    @Override
    @Transactional
    public ReservaDTO createReserva(ReservaDTO reservaDTO) {
        Reserva reserva = new Reserva();
        reserva.setUsuario(reservaDTO.getUsuario());
        reserva.setEvento(reservaDTO.getEvento());
        reserva.setConfirmado(reservaDTO.isConfirmado());
        Reserva savedReserva = reservaRepository.save(reserva);
        return convertToDTO(savedReserva);
    }

    @Override
    public List<ReservaDTO> getAllReservas() {
        return reservaRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ReservaDTO> getReservaById(Long id) {
        return reservaRepository.findById(id).map(this::convertToDTO);
    }

    @Override
    @Transactional
    public ReservaDTO updateReserva(Long id, ReservaDTO reservaDTO) {
        Reserva reserva = reservaRepository.findById(id).orElse(null);
        if (reserva != null) {
            reserva.setUsuario(reservaDTO.getUsuario());
            reserva.setEvento(reservaDTO.getEvento());
            reserva.setConfirmado(reservaDTO.isConfirmado());
            Reserva savedReserva = reservaRepository.save(reserva);
            return convertToDTO(savedReserva);
        }
        return null;
    }

    @Override
    @Transactional
    public void deleteReservaById(Long id) {
        reservaRepository.deleteById(id);
    }

    private ReservaDTO convertToDTO(Reserva reserva) {
        return new ReservaDTO(
                reserva.getIdReserva(),
                reserva.getUsuario(),
                reserva.getEvento(),
                reserva.isConfirmado()
        );
    }
}