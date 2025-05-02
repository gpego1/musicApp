package br.com.project.music.services.impl;
import br.com.project.music.business.dtos.ReservaDTO;
import br.com.project.music.business.entities.Reserva;
import br.com.project.music.business.entities.User;
import br.com.project.music.business.entities.Event;
import br.com.project.music.business.repositories.ReservaRepository;
import br.com.project.music.business.repositories.UserRepository;
import br.com.project.music.business.repositories.EventRepository;
import br.com.project.music.services.ReservaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReservaServiceImpl implements ReservaService {

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Override
    public ReservaDTO createReserva(ReservaDTO reservaDTO) {
        User user = userRepository.findById(reservaDTO.getUsuario().getId()).orElse(null);
        Event event = eventRepository.findById(reservaDTO.getEvento().getIdEvento()).orElse(null);
        if (user == null || event == null) {
            return null;
        }
        Reserva reserva = convertToEntity(reservaDTO, user, event);
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
        return reservaRepository.findById(id)
                .map(this::convertToDTO);
    }

    @Override
    public ReservaDTO updateReserva(Long id, ReservaDTO reservaDTO) {
        Optional<Reserva> existingReservaOptional = reservaRepository.findById(id);
        if (existingReservaOptional.isPresent()) {
            Reserva existingReserva = existingReservaOptional.get();
            User user = userRepository.findById(reservaDTO.getUsuario().getId()).orElse(existingReserva.getUsuario());
            Event event = eventRepository.findById(reservaDTO.getEvento().getIdEvento()).orElse(existingReserva.getEvento());
            Reserva updatedReserva = convertToEntity(reservaDTO, user, event);
            updatedReserva.setIdReserva(id);
            Reserva savedReserva = reservaRepository.save(updatedReserva);
            return convertToDTO(savedReserva);
        }
        return null; // Handle not found scenario
    }
    @Override
    public void deleteReservaById(Long id) {
        reservaRepository.deleteById(id);
    }
    @Override
    public List<ReservaDTO> getReservasByUsuario(Long userId) {
        return userRepository.findById(userId)
                .map(user -> reservaRepository.findByUsuario(user).stream()
                        .map(this::convertToDTO)
                        .collect(Collectors.toList()))
                .orElse(null); // Handle user not found
    }
    @Override
    public List<ReservaDTO> getReservasByEvento(Long eventoId) {
        return eventRepository.findById(eventoId)
                .map(event -> reservaRepository.findByEvento(event).stream()
                        .map(this::convertToDTO)
                        .collect(Collectors.toList()))
                .orElse(null); // Handle event not found
    }
    @Override
    public List<ReservaDTO> getReservasByUserAndEvent(Long userId, Long eventoId) {
        return userRepository.findById(userId)
                .flatMap(user -> eventRepository.findById(eventoId)
                        .map(event -> reservaRepository.findByUsuarioAndEvento(user, event).stream()
                                .map(this::convertToDTO)
                                .collect(Collectors.toList())))
                .orElse(null); // Handle user or event not found
    }
    private ReservaDTO convertToDTO(Reserva reserva) {
        ReservaDTO dto = new ReservaDTO();
        dto.setIdReserva(reserva.getIdReserva());
        dto.setUsuario(reserva.getUsuario());
        dto.setEvento(reserva.getEvento());
        dto.setConfirmado(reserva.isConfirmado());
        return dto;
    }
    private Reserva convertToEntity(ReservaDTO dto, User user, Event event) {
        Reserva reserva = new Reserva();
        reserva.setIdReserva(dto.getIdReserva());
        reserva.setUsuario(user);
        reserva.setEvento(event);
        reserva.setConfirmado(dto.isConfirmado());
        return reserva;
    }
}