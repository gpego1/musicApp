package br.com.project.music.controllers;

import br.com.project.music.business.dtos.MusicoDTO;
import br.com.project.music.services.MusicoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/musicos")
public class MusicoController {
    private final MusicoService musicoService;

    @Autowired
    public MusicoController(MusicoService musicoService) {
        this.musicoService = musicoService;
    }

    @GetMapping
    public ResponseEntity<List<MusicoDTO>> getAllMusicos() {
        List<MusicoDTO> musicos = musicoService.getAllMusicos();
        return new ResponseEntity<>(musicos, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MusicoDTO> getMusicoById(@PathVariable Long id) {
        Optional<MusicoDTO> musicoOptional = musicoService.getMusicoById(id);
        if (musicoOptional.isPresent()) {
            return new ResponseEntity<>(musicoOptional.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping
    public ResponseEntity<MusicoDTO> createMusico(@RequestBody MusicoDTO musico) {
        MusicoDTO musicoCriado = musicoService.createMusico(musico);
        return new ResponseEntity<>(musicoCriado, HttpStatus.CREATED);
    }
    @PutMapping("/{id}")
    public ResponseEntity<MusicoDTO> updateMusico(@PathVariable Long id, @RequestBody MusicoDTO musicoDTO) {
        MusicoDTO updatedMusico = musicoService.updateMusico(id, musicoDTO);
        if (updatedMusico != null) {
            return new ResponseEntity<>(updatedMusico, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMusico(@PathVariable Long id) {
        musicoService.deleteMusico(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
