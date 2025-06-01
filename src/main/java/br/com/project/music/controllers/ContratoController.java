package br.com.project.music.controllers;

import br.com.project.music.business.entities.Contrato;
import br.com.project.music.business.entities.Contrato.ContratoId;
import br.com.project.music.business.entities.Event;
import br.com.project.music.business.entities.Musico;
import br.com.project.music.exceptions.ResourceNotFoundException;
import br.com.project.music.services.ContratoService;
import br.com.project.music.business.repositories.EventRepository; // Adicionado para buscar Event
import br.com.project.music.business.repositories.MusicoRepository; // Adicionado para buscar Musico

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/contratos")
@Tag(name = "Contratos", description = "Gerenciamento de contratos entre músicos e eventos")
public class ContratoController {

    @Autowired
    private ContratoService contratoService;

    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private MusicoRepository musicoRepository;

    @GetMapping
    public ResponseEntity<List<Contrato>> getAllContratos() {
        List<Contrato> contratos = contratoService.findAll();
        return ResponseEntity.ok(contratos);
    }

    @GetMapping("/{idEvento}/{idMusico}")
    public ResponseEntity<Object> getContratoById(
            @PathVariable Long idEvento,
            @PathVariable Long idMusico) {
        try {
            Contrato contrato = contratoService.getContratoByEventoAndMusico(idEvento, idMusico);
            return ResponseEntity.ok(contrato);
        } catch (ResourceNotFoundException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Ocorreu um erro interno: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/musico/{musicoId}")
    @Transactional
    public ResponseEntity<List<Contrato>> getContratoByMusicoId(@PathVariable Long musicoId){
        List<Contrato> contratosMusico = contratoService.findByMusicoId(musicoId);
        if(contratosMusico.isEmpty()){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(contratosMusico);
    }

    @PostMapping
    public ResponseEntity<Object> createContrato(@RequestBody Contrato contrato) { // Recebe Contrato diretamente
        try {
            // No POST, o Jackson já deve ter populado o ContratoId com objetos Event e Musico
            // que contêm apenas os IDs. O Service irá buscar as entidades gerenciadas.
            Contrato savedContrato = contratoService.save(contrato);
            return new ResponseEntity<>(savedContrato, HttpStatus.CREATED);
        } catch (ResourceNotFoundException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Ocorreu um erro interno: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PutMapping("/{idEvento}/{idMusico}")
    public ResponseEntity<Object> updateContrato(
            @PathVariable Long idEvento,
            @PathVariable Long idMusico,
            @RequestBody Contrato contratoDetails) { // Recebe Contrato diretamente

        try {
            // Busca as entidades gerenciadas de Evento e Músico pelos IDs
            Event evento = eventRepository.findById(idEvento)
                    .orElseThrow(() -> new ResourceNotFoundException("Evento não encontrado com ID: " + idEvento));
            Musico musico = musicoRepository.findById(idMusico)
                    .orElseThrow(() -> new ResourceNotFoundException("Músico não encontrado com ID: " + idMusico));

            // Cria o ContratoId para buscar o contrato existente
            ContratoId contratoId = new ContratoId(evento, musico);

            // Tenta buscar o contrato existente
            Contrato contratoToUpdate = contratoService.findById(contratoId)
                    .orElseThrow(() -> new ResourceNotFoundException("Contrato não encontrado para Evento ID: " + idEvento + " e Músico ID: " + idMusico));

            // Atualiza os campos do contrato existente com os dados recebidos
            contratoToUpdate.setValor(contratoDetails.getValor());
            contratoToUpdate.setDetalhes(contratoDetails.getDetalhes());
            // O status pode ser atualizado se vier no corpo da requisição
            contratoToUpdate.setStatus(contratoDetails.isStatus());

            Contrato updatedContrato = contratoService.save(contratoToUpdate);
            return ResponseEntity.ok(updatedContrato);
        } catch (ResourceNotFoundException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Ocorreu um erro interno: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PutMapping("/{idEvento}/{idMusico}/activate")
    public ResponseEntity<Object> ativarContrato(@PathVariable Long idEvento, @PathVariable Long idMusico){
        try {
            // Busca as entidades gerenciadas de Evento e Músico pelos IDs
            Event evento = eventRepository.findById(idEvento)
                    .orElseThrow(() -> new ResourceNotFoundException("Evento não encontrado com ID: " + idEvento));
            Musico musico = musicoRepository.findById(idMusico)
                    .orElseThrow(() -> new ResourceNotFoundException("Músico não encontrado com ID: " + idMusico));

            ContratoId id = new ContratoId(evento, musico);
            Contrato activeContrato = contratoService.activateContrato(id);
            return ResponseEntity.ok(activeContrato);
        } catch(ResourceNotFoundException e){
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch(IllegalArgumentException e){
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Ocorreu um erro interno: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @DeleteMapping("/{idEvento}/{idMusico}")
    public ResponseEntity<Object> deleteContrato(@PathVariable Long idEvento, @PathVariable Long idMusico) {
        try {
            // Busca as entidades gerenciadas de Evento e Músico pelos IDs
            Event evento = eventRepository.findById(idEvento)
                    .orElseThrow(() -> new ResourceNotFoundException("Evento não encontrado com ID: " + idEvento));
            Musico musico = musicoRepository.findById(idMusico)
                    .orElseThrow(() -> new ResourceNotFoundException("Músico não encontrado com ID: " + idMusico));

            ContratoId id = new ContratoId(evento, musico);

            // Verifica se o contrato existe antes de tentar deletar
            if (!contratoService.existsById(id)) {
                throw new ResourceNotFoundException("Contrato não encontrado para Evento ID: " + idEvento + " e Músico ID: " + idMusico);
            }
            contratoService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Ocorreu um erro interno: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}