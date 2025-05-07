package br.com.project.music.business.repositories;

import br.com.project.music.business.entities.Escala;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EscalaRepository extends JpaRepository<Escala, Escala.EscalaId> {

}
