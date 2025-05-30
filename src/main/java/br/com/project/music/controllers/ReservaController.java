// ReservaController.java
package br.com.project.music.controllers;

import br.com.project.music.business.dtos.ReservaDTO;
import br.com.project.music.business.entities.Reserva;
import br.com.project.music.business.repositories.ReservaRepository;
import br.com.project.music.services.ReservaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/reservas")
@Tag(name="Reservas", description = "Gerenciamento de reservas para eventos")

public class ReservaController {

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private ReservaRepository reservaRepository;

    @PostMapping
    public ResponseEntity<ReservaDTO> createReserva(@RequestBody ReservaDTO reservaDTO) {
        ReservaDTO createdReserva = reservaService.createReserva(reservaDTO);
        if (createdReserva != null) {
            return new ResponseEntity<>(createdReserva, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
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
    public ResponseEntity<ReservaDTO> updateReserva(@PathVariable Long id, @RequestBody ReservaDTO reservaDTO) {
        ReservaDTO updatedReserva = reservaService.updateReserva(id, reservaDTO);
        if (updatedReserva != null) {
            return new ResponseEntity<>(updatedReserva, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservaById(@PathVariable Long id) {
        reservaService.deleteReservaById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/usuario/{userId}")
    public ResponseEntity<List<ReservaDTO>> getReservasByUsuario(@PathVariable Long userId) {
        List<ReservaDTO> reservas = reservaService.getReservasByUsuario(userId);
        if (reservas != null) {
            return new ResponseEntity<>(reservas, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    @GetMapping("/usuario/{userId}/confirmadas")
    public ResponseEntity<List<ReservaDTO>> getConfirmedReservasByUsuario(@PathVariable Long userId){
        List<ReservaDTO> confirmedReservas = reservaService.getConfirmedReservasByUsuario(userId);

        if(confirmedReservas == null){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if(confirmedReservas.isEmpty()){
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(confirmedReservas, HttpStatus.OK);
    }
    @GetMapping("/user/{userId}/confirmed/past")
    public ResponseEntity<List<Reserva>> getReservasPassadasConfirmadasByUsuario(@PathVariable("userId") Long userId){
        if(userId == null){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        LocalDateTime now = LocalDateTime.now();
        List<Reserva> reservas = reservaRepository.findByUsuarioIdAndEventoDataHoraBeforeAndConfirmadoTrue(userId, now);
        if(reservas == null){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(reservas, HttpStatus.OK);
    }

    @GetMapping("/evento/{eventoId}")
    public ResponseEntity<List<ReservaDTO>> getReservasByEvento(@PathVariable Long eventoId) {
        List<ReservaDTO> reservas = reservaService.getReservasByEvento(eventoId);
        if (reservas != null) {
            return new ResponseEntity<>(reservas, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}