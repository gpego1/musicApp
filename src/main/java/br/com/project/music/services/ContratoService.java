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
        return contratoRepository.findAllWithEventAndMusico();
    }

    public Optional<Contrato> findById(ContratoId id) {
        return contratoRepository.findById(id);
    }

    @Transactional
    public Contrato save(Contrato contrato) {
        Event evento = eventRepository.findById(contrato.getIdContrato().getEvento().getIdEvento())
                .orElseThrow(() -> new ResourceNotFoundException("Evento não encontrado com ID: " + contrato.getIdContrato().getEvento().getIdEvento()));
        Musico musico = musicoRepository.findById(contrato.getIdContrato().getMusico().getIdMusico())
                .orElseThrow(() -> new ResourceNotFoundException("Músico não encontrado com ID: " + contrato.getIdContrato().getMusico().getIdMusico()));

        contrato.getIdContrato().setEvento(evento);
        contrato.getIdContrato().setMusico(musico);

        if (!evento.isContractTimeWithinEvent(contrato.getHorarioInicio(), contrato.getHorarioFim())) {
            throw new IllegalArgumentException("Horário do contrato (" + contrato.getHorarioInicio() + " - " + contrato.getHorarioFim() +
                    ") está fora do período do evento (" + evento.getDataHora() +
                    " - " + evento.getHoraEncerramento() + ").");
        }

        List<Contrato> contratosSobrepostos = contratoRepository.findByMusicoAndOverlappingPeriod(
                musico, contrato.getHorarioInicio(), contrato.getHorarioFim());

        for (Contrato outroContrato : contratosSobrepostos) {
            if (!outroContrato.getIdContrato().equals(contrato.getIdContrato())) {
                throw new IllegalArgumentException("O músico " + musico.getNomeArtistico() +
                        " já possui um contrato sobreposto: Evento '" + outroContrato.getIdContrato().getEvento().getNomeEvento() +
                        "' (" + outroContrato.getHorarioInicio() + " - " + outroContrato.getHorarioFim() + ").");
            }
        }
        evento.getContratosDoEvento().add(contrato);
        musico.getContratos().add(contrato);

        return contratoRepository.save(contrato);
    }

    @Transactional
    public void deleteById(ContratoId id) {
        contratoRepository.deleteById(id);
    }

    public boolean existsById(ContratoId id) {
        return contratoRepository.existsById(id);
    }
    @Transactional(readOnly = true)
    public List<Contrato> findByMusicoId(Long musicoId) {
        Musico musico = musicoRepository.findById(musicoId)
                .orElseThrow(() -> new ResourceNotFoundException("Músico não encontrado com ID: " + musicoId));
        return musico.getContratos();
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
    public Contrato getHorarioByEventoAndMusico(Long eventoId, Long musicoId) {
        Event evento = eventRepository.findById(eventoId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento não encontrado com ID: " + eventoId));
        Musico musico = musicoRepository.findById(musicoId)
                .orElseThrow(() -> new ResourceNotFoundException("Músico não encontrado com ID: " + musicoId));

        ContratoId id = new ContratoId(evento, musico);

        return contratoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato não encontrado para o Evento " + eventoId + " e Músico " + musicoId));
    }
}