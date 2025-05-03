package br.com.project.music.services;

import br.com.project.music.business.entities.Avaliacao;
import br.com.project.music.business.entities.Event;
import br.com.project.music.business.entities.User;
import br.com.project.music.business.repositories.AvaliacaoRepository;
import br.com.project.music.business.repositories.EventRepository;
import br.com.project.music.business.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AvaliacaoService {

    private final AvaliacaoRepository avaliacaoRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Autowired
    public AvaliacaoService(AvaliacaoRepository avaliacaoRepository, UserRepository userRepository, EventRepository eventRepository) {
        this.avaliacaoRepository = avaliacaoRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
    }
    public List<Avaliacao> getAllAvaliacoes() {return avaliacaoRepository.findAll();}

    public Avaliacao criarAvaliacao(Long usuarioId, Long eventoId, int nota, String mensagem) {
        User usuario = userRepository.findById(usuarioId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com ID: " + usuarioId));
        Event evento = eventRepository.findById(eventoId)
                .orElseThrow(() -> new EntityNotFoundException("Evento não encontrado com ID: " + eventoId));

        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setUsuario(usuario);
        avaliacao.setEvento(evento);
        avaliacao.setNota(nota);
        avaliacao.setMensagem(mensagem);

        return avaliacaoRepository.save(avaliacao);
    }

    public Avaliacao buscarAvaliacaoPorId(Long id) {
        return avaliacaoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Avaliação não encontrada com ID: " + id));
    }

    public List<Avaliacao> buscarAvaliacoesPorEvento(Long eventoId) {
        return avaliacaoRepository.findByEvento_IdEvento(eventoId);
    }

    public List<Avaliacao> buscarAvaliacoesPorUsuario(Long usuarioId) {
        return avaliacaoRepository.findByUsuario_Id(usuarioId);
    }

    public List<Avaliacao> buscarAvaliacoesPorEventoOrdenadasPorNotaDescendente(Long eventoId) {
        return avaliacaoRepository.findByEvento_IdEventoOrderByNotaDesc(eventoId);
    }

    public List<Avaliacao> buscarAvaliacoesPorEventoOrdenadasPorNotaAscendente(Long eventoId) {
        return avaliacaoRepository.findByEvento_IdEventoOrderByNotaAsc(eventoId);
    }

    public void deletarAvaliacao(Long id) {
        if (!avaliacaoRepository.existsById(id)) {
            throw new EntityNotFoundException("Avaliação não encontrada com ID: " + id);
        }
        avaliacaoRepository.deleteById(id);
    }

    // Outras lógicas de negócio relacionadas a Avaliacao podem ser adicionadas aqui
}