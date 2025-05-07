package br.com.project.music.business.repositories;

import br.com.project.music.business.entities.Contrato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContratoRepository extends JpaRepository<Contrato, Contrato.ContratoId> {
}
