package br.com.project.music.controllers;

import br.com.project.music.business.dtos.AvaliacaoRequestDTO;
import br.com.project.music.business.entities.Avaliacao;
import br.com.project.music.services.AvaliacaoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/avaliacoes")
public class AvaliacaoController {

    private final AvaliacaoService avaliacaoService;

    @Autowired
    public AvaliacaoController(AvaliacaoService avaliacaoService) {
        this.avaliacaoService = avaliacaoService;
    }
    @GetMapping
    public ResponseEntity<List<Avaliacao>> getAllAvaliacoes() {
        List<Avaliacao> avaliacoes = avaliacaoService.getAllAvaliacoes();
        return new ResponseEntity<>(avaliacoes, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Avaliacao> criarAvaliacao(@RequestBody @Valid AvaliacaoRequestDTO avaliacaoRequest) {
        try {
            Avaliacao avaliacao = avaliacaoService.criarAvaliacao(
                    avaliacaoRequest.getUsuarioId(),
                    avaliacaoRequest.getEventoId(),
                    avaliacaoRequest.getNota(),
                    avaliacaoRequest.getMensagem()
            );
            return new ResponseEntity<>(avaliacao, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Avaliacao> buscarAvaliacaoPorId(@PathVariable Long id) {
        try {
            Avaliacao avaliacao = avaliacaoService.buscarAvaliacaoPorId(id);
            return new ResponseEntity<>(avaliacao, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/evento/{eventoId}")
    public ResponseEntity<List<Avaliacao>> buscarAvaliacoesPorEvento(@PathVariable Long eventoId) {
        List<Avaliacao> avaliacoes = avaliacaoService.buscarAvaliacoesPorEvento(eventoId);
        return new ResponseEntity<>(avaliacoes, HttpStatus.OK);
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<Avaliacao>> buscarAvaliacoesPorUsuario(@PathVariable Long usuarioId) {
        List<Avaliacao> avaliacoes = avaliacaoService.buscarAvaliacoesPorUsuario(usuarioId);
        return new ResponseEntity<>(avaliacoes, HttpStatus.OK);
    }

    @GetMapping("/evento/{eventoId}/ordenar/desc")
    public ResponseEntity<List<Avaliacao>> buscarAvaliacoesPorEventoOrdenadasDescendente(@PathVariable Long eventoId) {
        List<Avaliacao> avaliacoes = avaliacaoService.buscarAvaliacoesPorEventoOrdenadasPorNotaDescendente(eventoId);
        return new ResponseEntity<>(avaliacoes, HttpStatus.OK);
    }

    @GetMapping("/evento/{eventoId}/ordenar/asc")
    public ResponseEntity<List<Avaliacao>> buscarAvaliacoesPorEventoOrdenadasAscendente(@PathVariable Long eventoId) {
        List<Avaliacao> avaliacoes = avaliacaoService.buscarAvaliacoesPorEventoOrdenadasPorNotaAscendente(eventoId);
        return new ResponseEntity<>(avaliacoes, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarAvaliacao(@PathVariable Long id) {
        try {
            avaliacaoService.deletarAvaliacao(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Outros endpoints para atualizar avaliações podem ser adicionados aqui
}