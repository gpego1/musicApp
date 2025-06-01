package br.com.project.music.business.repositories;
import br.com.project.music.business.entities.Contrato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ContratoRepository extends JpaRepository<Contrato, Contrato.ContratoId> {

    List<Contrato> findByIdContrato_Musico_IdMusico(Long idMusico);

}
