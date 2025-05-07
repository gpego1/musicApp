package br.com.project.music.controllers;

import br.com.project.music.business.entities.Escala;
import br.com.project.music.business.entities.Escala.EscalaId;
import br.com.project.music.business.entities.Event;
import br.com.project.music.business.entities.Genre;
import br.com.project.music.business.entities.Musico;
import br.com.project.music.services.EscalaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/escalas")
public class EscalaController {

    @Autowired
    private EscalaService escalaService;

    @GetMapping
    public ResponseEntity<List<Escala>> getAllEscalas() {
        List<Escala> escalas = escalaService.findAll();
        return ResponseEntity.ok(escalas);
    }

    @GetMapping("/{idEvento}/{idGeneroMusical}")
    public ResponseEntity<Escala> getEscalaById(
            @PathVariable Long idEvento,
            @PathVariable Long idGeneroMusical) {
        EscalaId id = new EscalaId();
        Event evento = new Event();
        evento.setIdEvento(idEvento);
        Genre genero = new Genre();
        genero.setIdGeneroMusical(idGeneroMusical);
        id.setEvento(evento);
        id.setGenero(genero);

        Optional<Escala> escala = escalaService.findById(id);
        return escala.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Escala> createEscala(@RequestBody Escala escala) {
        Escala savedEscala = escalaService.save(escala);
        return new ResponseEntity<>(savedEscala, HttpStatus.CREATED);
    }

    @PutMapping("/{idEvento}/{idGeneroMusical}")
    public ResponseEntity<Escala> updateEscala(
            @PathVariable Long idEvento,
            @PathVariable Long idGeneroMusical,
            @RequestBody Escala escalaDetails) {
        EscalaId id = new EscalaId();
        Event evento = new Event();
        evento.setIdEvento(idEvento);
        Genre genero = new Genre();
        genero.setIdGeneroMusical(idGeneroMusical);
        id.setEvento(evento);
        id.setGenero(genero);

        Optional<Escala> existingEscala = escalaService.findById(id);
        if (existingEscala.isPresent()) {
            escalaDetails.setIdEscala(id); // Garante que o ID seja o correto para a atualização
            Escala updatedEscala = escalaService.save(escalaDetails);
            return ResponseEntity.ok(updatedEscala);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{idEvento}/{idGeneroMusical}")
    public ResponseEntity<Void> deleteEscala(
            @PathVariable Long idEvento,
            @PathVariable Long idGeneroMusical) {
        EscalaId id = new EscalaId();
        Event evento = new Event();
        evento.setIdEvento(idEvento);
        Genre genero = new Genre();
        genero.setIdGeneroMusical(idGeneroMusical);
        id.setEvento(evento);
        id.setGenero(genero);

        if (escalaService.existsById(id)) {
            escalaService.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}