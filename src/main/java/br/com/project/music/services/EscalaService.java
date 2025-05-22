package br.com.project.music.services;

import br.com.project.music.business.entities.Escala;
import br.com.project.music.business.entities.Escala.EscalaId;
import br.com.project.music.business.repositories.EscalaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EscalaService {

    @Autowired
    private EscalaRepository escalaRepository;

    public List<Escala> findAll() {
        return escalaRepository.findAll();
    }

    public Optional<Escala> findById(EscalaId id) {
        return escalaRepository.findById(id);
    }

    public Escala save(Escala escala) {
        return escalaRepository.save(escala);
    }

    public void deleteById(EscalaId id) {
        escalaRepository.deleteById(id);
    }

    public boolean existsById(EscalaId id) {
        return escalaRepository.existsById(id);
    }

    public List<Escala> getEscalasByEventId(Long eventId) {return escalaRepository.findByIdEscala_Evento_IdEvento(eventId);}
}