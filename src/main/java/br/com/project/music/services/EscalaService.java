package br.com.project.music.services;

import br.com.project.music.business.entities.Escala;
import br.com.project.music.business.entities.Escala.EscalaId;
import br.com.project.music.business.entities.Musico;
import br.com.project.music.business.repositories.EscalaRepository;
import jakarta.transaction.Transactional; // Use este import para @Transactional
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EscalaService {

    @Autowired
    private EscalaRepository escalaRepository;

    @Transactional
    public List<Escala> findAll() {
        return escalaRepository.findAllWithMusicos();
    }

    @Transactional
    public Optional<Escala> findById(EscalaId id) {
        return escalaRepository.findByIdWithMusicos(id);
    }

    @Transactional
    public List<Escala> getEscalasByEventId(Long eventId) {
        return escalaRepository.findEscalasByEventIdWithMusicos(eventId);
    }

    @Transactional // A anotação @Transactional é importante para operações de escrita
    public Escala createOrUpdateEscala(Escala novaEscala) {
        if (novaEscala.getIdEscala() == null ||
                novaEscala.getIdEscala().getEvento() == null ||
                novaEscala.getIdEscala().getGenero() == null) {
            throw new IllegalArgumentException("O ID da escala, evento e gênero musical não podem ser nulos!");
        }
        Optional<Escala> existingEscalaOptional = escalaRepository.findById(novaEscala.getIdEscala());
        if (existingEscalaOptional.isPresent()) {
            Escala existingEscala = existingEscalaOptional.get();

            List<Long> existingMusicianIds = existingEscala.getMusicos().stream()
                    .map(Musico::getIdMusico)
                    .collect(Collectors.toList());

            for (Musico newMusico : novaEscala.getMusicos()) {
                if (newMusico.getIdMusico() == null) {
                    throw new IllegalArgumentException("O ID do músico não pode ser nulo ao adicionar à escala!");
                }
                if (!existingMusicianIds.contains(newMusico.getIdMusico())) {
                    existingEscala.getMusicos().add(newMusico);
                } else {
                    System.out.println("Músico com ID " + newMusico.getIdMusico() + " já está na escala. Pulando duplicação.");
                }
            }
            return escalaRepository.save(existingEscala);
        } else {
            return escalaRepository.save(novaEscala);
        }
    }

    @Transactional
    public Escala save(Escala escala) {
        return escalaRepository.save(escala);
    }

    @Transactional
    public void deleteById(EscalaId id) {
        escalaRepository.deleteById(id);
    }

    @Transactional
    public boolean existsById(EscalaId id) {
        return escalaRepository.existsById(id);
    }
}