package br.com.project.music.controllers;
import br.com.project.music.business.dtos.EscalaRequestDTO;
import br.com.project.music.business.dtos.EscalaResponseDTO; // Importe o novo DTO
import br.com.project.music.business.entities.Escala;
import br.com.project.music.business.entities.Escala.EscalaId;
import br.com.project.music.business.entities.Event;
import br.com.project.music.business.entities.Genre;
import br.com.project.music.business.entities.Musico;
import br.com.project.music.business.repositories.EscalaRepository;
import br.com.project.music.business.repositories.EventRepository;
import br.com.project.music.business.repositories.GenresRepository;
import br.com.project.music.business.repositories.MusicoRepository;
import br.com.project.music.services.EscalaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/escalas")
@Tag(name="Escalas", description = "Gerenciamento de escalas de músicos para eventos")
public class EscalaController {

    @Autowired
    private EscalaService escalaService;

    @Autowired
    private EscalaRepository escalaRepository;

    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private GenresRepository genreRepository;
    @Autowired
    private MusicoRepository musicoRepository;

    @GetMapping
    public ResponseEntity<List<EscalaResponseDTO>> getAllEscalas() {
        List<Escala> escalas = escalaService.findAll();
        List<EscalaResponseDTO> dtos = escalas.stream()
                .map(EscalaResponseDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{idEvento}/{idGeneroMusical}")
    public ResponseEntity<EscalaResponseDTO> getEscalaById(@PathVariable Long idEvento, @PathVariable Long idGeneroMusical) {
        Event evento = eventRepository.findById(idEvento)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento não encontrado com ID: " + idEvento));
        Genre genero = genreRepository.findById(idGeneroMusical)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Gênero Musical não encontrado com ID: " + idGeneroMusical));

        EscalaId id = new EscalaId(evento, genero);
        Optional<Escala> escala = escalaService.findById(id);
        return escala.map(e -> ResponseEntity.ok(new EscalaResponseDTO(e)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<EscalaResponseDTO>> getEscalasByEvent(@PathVariable Long eventId) {
        List<Escala> escalas = escalaService.getEscalasByEventId(eventId);
        if (escalas.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        List<EscalaResponseDTO> dtos = escalas.stream()
                .map(EscalaResponseDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    @Transactional
    public ResponseEntity<EscalaResponseDTO> createEscala(@RequestBody EscalaRequestDTO request) {
        try {
            Event evento = eventRepository.findById(request.getIdEvento())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento não encontrado com ID: " + request.getIdEvento()));
            Genre genero = genreRepository.findById(request.getIdGeneroMusical())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Gênero Musical não encontrado com ID: " + request.getIdGeneroMusical()));

            EscalaId escalaIdentificador = new EscalaId(evento, genero);
            Optional<Escala> existingEscalaOptional = escalaRepository.findById(escalaIdentificador);
            Escala escalaToPersist;
            Set<Long> allMusicianIds = new HashSet<>();

            if (existingEscalaOptional.isPresent()) {
                escalaToPersist = existingEscalaOptional.get();

                if (escalaToPersist.getMusicos() != null) {
                    escalaToPersist.getMusicos().stream()
                            .map(Musico::getIdMusico)
                            .forEach(allMusicianIds::add);
                }
                System.out.println("Escala existente encontrada. Músicos atuais (IDs): " + allMusicianIds);

            } else {
                escalaToPersist = new Escala();
                escalaToPersist.setIdEscala(escalaIdentificador);
                System.out.println("Nenhuma escala existente encontrada. Criando nova escala.");
            }
            if (request.getIdsMusicos() != null && !request.getIdsMusicos().isEmpty()) {
                request.getIdsMusicos().forEach(allMusicianIds::add);
            }
            System.out.println("IDs de músicos da requisição adicionados. Total de IDs para a escala: " + allMusicianIds);
            List<Musico> mergedMusicos = allMusicianIds.stream()
                    .map(musicoId -> musicoRepository.findById(musicoId)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Músico com ID " + musicoId + " não encontrado durante o merge de escala")))
                    .collect(Collectors.toList());

            escalaToPersist.setMusicos(mergedMusicos);
            System.out.println("Lista final de músicos para salvar na escala: " + mergedMusicos.size());
            Escala savedEscala = escalaService.save(escalaToPersist);
            return ResponseEntity.status(existingEscalaOptional.isPresent() ? HttpStatus.OK : HttpStatus.CREATED).body(new EscalaResponseDTO(savedEscala));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(null);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
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

        Event evento = eventRepository.findById(idEvento)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento não encontrado com ID: " + idEvento));
        Genre genero = genreRepository.findById(idGeneroMusical)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Gênero Musical não encontrado com ID: " + idGeneroMusical));
        EscalaId id = new EscalaId(evento, genero);


        if (escalaService.existsById(id)) {
            escalaService.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}