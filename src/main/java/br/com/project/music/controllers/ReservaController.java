package br.com.project.music.controllers;

import br.com.project.music.business.dtos.ReservaDTO;
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

    @PostMapping
    public ResponseEntity<ReservaDTO> createReserva(@RequestBody ReservaDTO reservaDTO) {
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
    public ResponseEntity<ReservaDTO> updateReserva(@PathVariable Long id, @RequestBody ReservaDTO reservaDTO) {
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