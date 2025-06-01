package br.com.project.music.business.repositories;

import br.com.project.music.business.entities.Contrato;
import br.com.project.music.business.entities.Event;
import br.com.project.music.business.entities.Musico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContratoRepository extends JpaRepository<Contrato, Contrato.ContratoId> {

    @Query("SELECT c FROM Contrato c JOIN FETCH c.idContrato.evento JOIN FETCH c.idContrato.musico")
    List<Contrato> findAllWithEventAndMusico();

    @Query("SELECT c FROM Contrato c WHERE c.idContrato.musico = :musico " +
            "AND c.horarioFim > :periodoInicio AND c.horarioInicio < :periodoFim")
    List<Contrato> findByMusicoAndOverlappingPeriod(
            @Param("musico") Musico musico,
            @Param("periodoInicio") LocalDateTime periodoInicio,
            @Param("periodoFim") LocalDateTime periodoFim);

}
