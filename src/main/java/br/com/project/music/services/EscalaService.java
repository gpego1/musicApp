package br.com.project.music.services;
import br.com.project.music.business.entities.Escala;
import br.com.project.music.business.entities.Escala.EscalaId;
import br.com.project.music.business.repositories.EscalaRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

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