package br.com.project.music.controllers;

import br.com.project.music.business.dtos.EscalaRequestDTO;
import br.com.project.music.business.dtos.EscalaResponseDTO;
import br.com.project.music.business.entities.Escala;
import br.com.project.music.business.entities.Escala.EscalaId;
import br.com.project.music.business.entities.Event;
import br.com.project.music.business.entities.Genre;
import br.com.project.music.business.entities.Musico;
import br.com.project.music.business.repositories.EventRepository;
import br.com.project.music.business.repositories.GenresRepository;
import br.com.project.music.business.repositories.MusicoRepository;
import br.com.project.music.services.EscalaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/escalas")
public class EscalaController {

    @Autowired
    private EscalaService escalaService;

    @Autowired
    private EventRepository eventRepository;
    @Autowired

    private GenresRepository genreRepository;
    @Autowired

    private MusicoRepository musicoRepository;

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
    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<Escala>> getEscalasByEvent(@PathVariable Long eventId) {
        List<Escala> escalas = escalaService.getEscalasByEventId(eventId);
        if (escalas.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(escalas);
    }

    @PostMapping
    public ResponseEntity<?> createOrUpdateEscala(@RequestBody Escala escala) {
        try {
            Escala savedEscala = escalaService.createOrUpdateEscala(escala);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedEscala);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno do servidor: " + e.getMessage());
        }
    }

    @PutMapping("/{idEvento}/{idGeneroMusical}")
    public ResponseEntity<EscalaResponseDTO> updateEscala(
            @PathVariable Long idEvento,
            @PathVariable Long idGeneroMusical,
            @RequestBody EscalaRequestDTO request) {
        Event evento = eventRepository.findById(idEvento)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento não encontrado"));
        Genre genero = genreRepository.findById(idGeneroMusical)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Gênero Musical não encontrado"));

        EscalaId id = new EscalaId(evento, genero);

        Optional<Escala> existingEscala = escalaService.findById(id);
        if (existingEscala.isPresent()) {
            Escala escalaToUpdate = existingEscala.get();

            List<Musico> musicosParaAtualizar = request.getIdsMusicos().stream()
                    .map(musicoId -> musicoRepository.findById(musicoId)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Músico com ID " + musicoId + " não encontrado")))
                    .collect(Collectors.toList());
            escalaToUpdate.setMusicos(musicosParaAtualizar);

            Escala updatedEscala = escalaService.save(escalaToUpdate);
            return ResponseEntity.ok(new EscalaResponseDTO(updatedEscala));
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