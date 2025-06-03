package br.com.project.music.services.impl;
import br.com.project.music.business.dtos.ReservaDTO;
import br.com.project.music.business.entities.Reserva;
import br.com.project.music.business.entities.User;
import br.com.project.music.business.entities.Event;
import br.com.project.music.business.repositories.ReservaRepository;
import br.com.project.music.business.repositories.UserRepository;
import br.com.project.music.business.repositories.EventRepository;
import br.com.project.music.services.EmailService;
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

    @Autowired
    private EmailService emailService;

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
            existingReserva.setConfirmado(reservaDTO.isConfirmado());

            if(reservaDTO.getUsuario() != null && reservaDTO.getUsuario().getId() !=null){
                User user = (existingReserva.getUsuario() != null && existingReserva.getUsuario().getId().equals(reservaDTO.getUsuario().getId()))
                        ? existingReserva.getUsuario()
                        : userRepository.findById(reservaDTO.getUsuario().getId()).orElse(null);
                existingReserva.setUsuario(user);
            }

            if(reservaDTO.getEvento() != null && reservaDTO.getEvento().getIdEvento() !=null){
                Event evento = (existingReserva.getEvento() != null && existingReserva.getEvento().getIdEvento().equals(reservaDTO.getEvento().getIdEvento()))
                        ?existingReserva.getEvento()
                        :eventRepository.findById(reservaDTO.getEvento().getIdEvento()).orElse(null);
                  existingReserva.setEvento(evento);
            }
            Reserva updatedReserva = reservaRepository.save(existingReserva);

            if(updatedReserva.isConfirmado() && updatedReserva.getUsuario() != null && updatedReserva.getEvento() != null) {
                try {
                    String emailUsuario = updatedReserva.getUsuario().getEmail();
                    String nomeEvento = updatedReserva.getEvento().getNomeEvento();
                    if(emailUsuario != null && !emailUsuario.isEmpty() && nomeEvento != null && !nomeEvento.isEmpty()) {
                        emailService.sendEmail(emailUsuario, nomeEvento);
                        System.out.println("E-mail de confirmação enviado para: " + emailUsuario + " sobre o evento: " + nomeEvento);
                    } else {
                        System.out.println("E-mail ou nome do evento inválidos para envio de confirmação.");
                    }
                } catch (Exception e) {
                    System.out.println("Erro ao enviar e-mail de confirmação: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            return convertToDTO(updatedReserva);
        }
        return null;
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
                .orElse(null);
    }
    @Override
    public List<ReservaDTO> getReservasByEvento(Long eventoId) {
        return eventRepository.findById(eventoId)
                .map(event -> reservaRepository.findByEvento(event).stream()
                        .map(this::convertToDTO)
                        .collect(Collectors.toList()))
                .orElse(null);
    }
    public List<ReservaDTO> getConfirmedReservasByUsuario(Long userId){
        if(userRepository.findById(userId).isEmpty()){
            return null;
        }
        List<Reserva> confirmedReservas = reservaRepository.findByUsuarioIdAndConfirmadoTrue(userId);
        return confirmedReservas.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }


    public List<ReservaDTO> verificarReservasUsuarioEvento(Long usuarioId, Long eventoId) {
        return reservaRepository.findByUsuarioIdAndEventoId(usuarioId, eventoId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
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