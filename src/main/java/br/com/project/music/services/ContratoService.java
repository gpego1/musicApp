package br.com.project.music.services;
import br.com.project.music.business.entities.Contrato;
import br.com.project.music.business.entities.Contrato.ContratoId;
import br.com.project.music.business.entities.Event;
import br.com.project.music.business.entities.Musico;
import br.com.project.music.business.repositories.ContratoRepository;
import br.com.project.music.business.repositories.EventRepository;
import br.com.project.music.business.repositories.MusicoRepository;
import br.com.project.music.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class ContratoService {

    @Autowired
    private ContratoRepository contratoRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private MusicoRepository musicoRepository;

    public List<Contrato> findAll() {
        return contratoRepository.findAll();
    }

    public Optional<Contrato> findById(ContratoId id) {
        return contratoRepository.findById(id);
    }

    @Transactional
    public Contrato save(Contrato contrato) {
        Event evento = eventRepository.findById(contrato.getIdContrato().getEvento().getIdEvento()) // <-- ERROR HERE
                .orElseThrow(() -> new ResourceNotFoundException("Evento não encontrado com ID: " + contrato.getIdContrato().getEvento().getIdEvento()));
        Musico musico = musicoRepository.findById(contrato.getIdContrato().getMusico().getIdMusico()) // <-- ERROR HERE
                .orElseThrow(() -> new ResourceNotFoundException("Músico não encontrado com ID: " + contrato.getIdContrato().getMusico().getIdMusico()));


        contrato.getIdContrato().setEvento(evento);
        contrato.getIdContrato().setMusico(musico);

        return contratoRepository.save(contrato);
    }

    public List<Contrato> findByMusicoId(Long musicoId) {
        return contratoRepository.findByIdContrato_Musico_IdMusico(musicoId);
    }

    @Transactional
    public Contrato activateContrato(ContratoId id) {
        Contrato contrato = contratoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato não encontrado."));

        if (contrato.isStatus()) {
            throw new IllegalArgumentException("Contrato já está ativo.");
        }
        contrato.setStatus(true);
        return contratoRepository.save(contrato);
    }

    @Transactional(readOnly = true)
    public Contrato getContratoByEventoAndMusico(Long eventoId, Long musicoId) {
        Event evento = eventRepository.findById(eventoId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento não encontrado com ID: " + eventoId));
        Musico musico = musicoRepository.findById(musicoId)
                .orElseThrow(() -> new ResourceNotFoundException("Músico não encontrado com ID: " + musicoId));
        ContratoId id = new ContratoId(evento, musico);

        return contratoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato não encontrado para o Evento " + eventoId + " e Músico " + musicoId));
    }

    @Transactional
    public void deleteById(ContratoId id) {
        contratoRepository.deleteById(id);
    }

    public boolean existsById(ContratoId id) {
        return contratoRepository.existsById(id);
    }
}