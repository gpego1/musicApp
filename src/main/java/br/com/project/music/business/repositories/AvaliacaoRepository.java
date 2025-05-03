package br.com.project.music.business.repositories;

import br.com.project.music.business.entities.Avaliacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {
    List<Avaliacao> findByEvento_IdEvento(Long idEvento);
    List<Avaliacao> findByUsuario_Id(Long idUsuario);
    List<Avaliacao> findByEvento_IdEventoOrderByNotaDesc(Long idEvento);
    List<Avaliacao> findByEvento_IdEventoOrderByNotaAsc(Long idEvento);
}