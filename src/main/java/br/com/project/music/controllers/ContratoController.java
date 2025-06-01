package br.com.project.music.controllers;
import br.com.project.music.business.entities.Contrato;
import br.com.project.music.business.entities.Contrato.ContratoId;
import br.com.project.music.business.entities.Event;
import br.com.project.music.business.entities.Musico;
import br.com.project.music.exceptions.ResourceNotFoundException;
import br.com.project.music.services.ContratoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/contratos")
@Tag(name = "Contratos", description = "Gerenciamento de contratos entre músicos e eventos")

public class ContratoController {

    @Autowired
    private ContratoService contratoService;

    @GetMapping
    public ResponseEntity<List<Contrato>> getAllContratos() {
        List<Contrato> contratos = contratoService.findAll();
        return ResponseEntity.ok(contratos);
    }

    @GetMapping("/{idEvento}/{idMusico}")
    public ResponseEntity<Contrato> getContratoById(
            @PathVariable Long idEvento,
            @PathVariable Long idMusico) {
        ContratoId id = new ContratoId();
        Event evento = new Event();
        evento.setIdEvento(idEvento);
        Musico musico = new Musico();
        musico.setIdMusico(idMusico);
        id.setEvento(evento);
        id.setMusico(musico);

        try {
            Contrato contrato = contratoService.getHorarioByEventoAndMusico(idEvento, idMusico);
            return ResponseEntity.ok(contrato);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/musico/{musicoId}")
    public ResponseEntity<List<Contrato>> getContratoByMusicoId(@PathVariable Long musicoId){
        List<Contrato> contratosMusico = contratoService.findByMusicoId(musicoId);
        if(contratosMusico.isEmpty()){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(contratosMusico);
    }

    @PostMapping
    public ResponseEntity<Contrato> createContrato(@RequestBody Contrato contrato) {
        try {
            Contrato savedContrato = contratoService.save(contrato);
            return new ResponseEntity<>(savedContrato, HttpStatus.CREATED);
        } catch (IllegalArgumentException | ResourceNotFoundException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PutMapping("/{idEvento}/{idMusico}")
    public ResponseEntity<Contrato> updateContrato(
            @PathVariable Long idEvento,
            @PathVariable Long idMusico,
            @RequestBody Contrato contratoDetails) {

        ContratoId id = new ContratoId();
        Event eventoPlaceholder = new Event(); eventoPlaceholder.setIdEvento(idEvento);
        Musico musicoPlaceholder = new Musico(); musicoPlaceholder.setIdMusico(idMusico);
        id.setEvento(eventoPlaceholder);
        id.setMusico(musicoPlaceholder);

        contratoDetails.setIdContrato(id);
        contratoDetails.getIdContrato().getEvento().setIdEvento(idEvento);
        contratoDetails.getIdContrato().getMusico().setIdMusico(idMusico);

        try {
            Contrato updatedContrato = contratoService.save(contratoDetails);
            return ResponseEntity.ok(updatedContrato);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PutMapping("/{idEvento}/{idMusico}/activate")
    public ResponseEntity<Contrato> ativarContrato(@PathVariable Long idEvento, @PathVariable Long idMusico){
        ContratoId id = new ContratoId();
        Event evento = new Event();
        evento.setIdEvento(idEvento);
        Musico musico = new Musico();
        musico.setIdMusico(idMusico);
        id.setEvento(evento);
        id.setMusico(musico);

        try {
            Contrato activeContrato = contratoService.activateContrato(id);
            return ResponseEntity.ok(activeContrato);
        } catch(ResourceNotFoundException e){
            return ResponseEntity.notFound().build();
        } catch(IllegalArgumentException e){
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }
    }

    @DeleteMapping("/{idEvento}/{idMusico}")
    public ResponseEntity<Void> deleteContrato(@PathVariable Long idEvento, @PathVariable Long idMusico) {
        ContratoId id = new ContratoId();
        Event evento = new Event(); evento.setIdEvento(idEvento);
        Musico musico = new Musico(); musico.setIdMusico(idMusico);
        id.setEvento(evento);
        id.setMusico(musico);
        try {
            contratoService.getHorarioByEventoAndMusico(idEvento, idMusico);
            contratoService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/evento/{eventoId}/musico/{musicoId}/horario")
    public ResponseEntity<Map<String, LocalDateTime>> getHorarioContratoMusico(@PathVariable Long eventoId, @PathVariable Long musicoId) {
        try {
            Contrato contrato = contratoService.getHorarioByEventoAndMusico(eventoId, musicoId);
            Map<String, LocalDateTime> horario = new HashMap<>();
            horario.put("horarioInicio", contrato.getHorarioInicio());
            horario.put("horarioFim", contrato.getHorarioFim());
            return ResponseEntity.ok(horario);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}