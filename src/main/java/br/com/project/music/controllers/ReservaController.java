package br.com.project.music.controllers;

import br.com.project.music.business.dtos.ReservaDTO;
import br.com.project.music.business.dtos.EventDTO;
import br.com.project.music.business.dtos.UserDTO;
import br.com.project.music.business.entities.Event;
import br.com.project.music.business.entities.User;
import br.com.project.music.business.repositories.EventRepository;
import br.com.project.music.business.repositories.UserRepository;
import br.com.project.music.services.ReservaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/reservas")
public class ReservaController {

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private UserRepository userRepository; // Adicione o UserRepository

    @Autowired
    private EventRepository eventRepository; // Adicione o EventRepository

    @PostMapping
    public ResponseEntity<?> createReserva(@RequestBody ReservaDTO reservaDTO) {
        // Buscar o User completo pelo ID diretamente do repositório
        Optional<User> userOptional = userRepository.findById(reservaDTO.getUsuario().getId());
        if (!userOptional.isPresent()) {
            return new ResponseEntity<>("Usuário não encontrado", HttpStatus.BAD_REQUEST);
        }
        User user = userOptional.get();
        reservaDTO.setUsuario(user);
        // Se o googleId do User encontrado for null, ele permanecerá null no reservaDTO

        // Buscar o Event completo pelo ID diretamente do repositório
        Optional<Event> eventOptional = eventRepository.findById(reservaDTO.getEvento().getIdEvento());
        if (!eventOptional.isPresent()) {
            return new ResponseEntity<>("Evento não encontrado", HttpStatus.BAD_REQUEST);
        }
        Event event = eventOptional.get();
        reservaDTO.setEvento(event);

        ReservaDTO createdReserva = reservaService.createReserva(reservaDTO);
        return new ResponseEntity<>(createdReserva, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ReservaDTO>> getAllReservas() {
        List<ReservaDTO> reservas = reservaService.getAllReservas();
        return new ResponseEntity<>(reservas, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservaDTO> getReservaById(@PathVariable Long id) {
        Optional<ReservaDTO> reservaDTO = reservaService.getReservaById(id);
        return reservaDTO.map(dto -> new ResponseEntity<>(dto, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateReserva(@PathVariable Long id, @RequestBody ReservaDTO reservaDTO) {
        // Buscar o User completo pelo ID diretamente do repositório
        Optional<User> userOptional = userRepository.findById(reservaDTO.getUsuario().getId());
        if (!userOptional.isPresent()) {
            return new ResponseEntity<>("Usuário não encontrado", HttpStatus.BAD_REQUEST);
        }
        User user = userOptional.get();
        reservaDTO.setUsuario(user);
        // Se o googleId do User encontrado for null, ele permanecerá null no reservaDTO

        // Buscar o Event completo pelo ID diretamente do repositório
        Optional<Event> eventOptional = eventRepository.findById(reservaDTO.getEvento().getIdEvento());
        if (!eventOptional.isPresent()) {
            return new ResponseEntity<>("Evento não encontrado", HttpStatus.BAD_REQUEST);
        }
        Event event = eventOptional.get();
        reservaDTO.setEvento(event);

        ReservaDTO updatedReserva = reservaService.updateReserva(id, reservaDTO);
        if (updatedReserva != null) {
            return new ResponseEntity<>(updatedReserva, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReserva(@PathVariable Long id) {
        reservaService.deleteReservaById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}