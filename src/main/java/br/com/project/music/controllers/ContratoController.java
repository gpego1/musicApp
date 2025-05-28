package br.com.project.music.controllers;

import br.com.project.music.business.entities.Contrato;
import br.com.project.music.business.entities.Contrato.ContratoId;
import br.com.project.music.business.entities.Event;
import br.com.project.music.business.entities.Musico;
import br.com.project.music.exceptions.ResourceNotFoundException;
import br.com.project.music.services.ContratoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/contratos")
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
        ContratoId id = createContratoId(idEvento, idMusico);
        Optional<Contrato> contrato = contratoService.findById(id);
        return contrato.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/musico/{musicoId}")
    public ResponseEntity<List<Contrato>> getContratoByMusicoId(@PathVariable Long musicoId) {
        List<Contrato> contratosMusico = contratoService.findByMusicoId(musicoId);
        if(contratosMusico.isEmpty()){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(contratosMusico);
    }

    @PostMapping
    public ResponseEntity<Contrato> createContrato(@RequestBody Contrato contrato) {
        Contrato savedContrato = contratoService.save(contrato);
        return new ResponseEntity<>(savedContrato, HttpStatus.CREATED);
    }

    @PutMapping("/{idEvento}/{idMusico}")
    public ResponseEntity<Contrato> updateContrato(
            @PathVariable Long idEvento,
            @PathVariable Long idMusico,
            @RequestBody Contrato contratoDetails) {
        ContratoId id = createContratoId(idEvento, idMusico);

        Optional<Contrato> existingContrato = contratoService.findById(id);
        if (existingContrato.isPresent()) {
            contratoDetails.setIdContrato(id);
            Contrato updatedContrato = contratoService.save(contratoDetails);
            return ResponseEntity.ok(updatedContrato);
        } else {
            return ResponseEntity.notFound().build();
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
        ContratoId id = createContratoId(idEvento, idMusico);

        if (contratoService.existsById(id)) {
            contratoService.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{idEvento}/{idMusico}/aprovar")
    public ResponseEntity<Contrato> aprovarContrato(@PathVariable Long idEvento, @PathVariable Long idMusico) {
        ContratoId id = createContratoId(idEvento, idMusico);
        Optional<Contrato> contratoOptional = contratoService.updateStatusToTrue(id);
        return contratoOptional.map(contrato -> new ResponseEntity<>(contrato, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    private ContratoId createContratoId(Long idEvento, Long idMusico) {
        ContratoId id = new ContratoId();
        Event evento = new Event();
        evento.setIdEvento(idEvento);
        Musico musico = new Musico();
        musico.setIdMusico(idMusico);
        id.setEvento(evento);
        id.setMusico(musico);
        return id;
    }
}