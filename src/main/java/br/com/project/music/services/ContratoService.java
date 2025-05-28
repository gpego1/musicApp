package br.com.project.music.services;
import br.com.project.music.business.entities.Contrato;
import br.com.project.music.business.entities.Contrato.ContratoId;
import br.com.project.music.business.repositories.ContratoRepository;
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

    public List<Contrato> findAll() {return contratoRepository.findAll();}

    public Optional<Contrato> findById(ContratoId id) {return contratoRepository.findById(id);}

    public Contrato save(Contrato contrato) {return contratoRepository.save(contrato);}

    public void deleteById(ContratoId id) {contratoRepository.deleteById(id);}

    public boolean existsById(ContratoId id) {return contratoRepository.existsById(id);}

    public List<Contrato> findByMusicoId(Long musicoId){
        return contratoRepository.findByIdContrato_Musico_IdMusico(musicoId);
    }

    @Transactional
    public Contrato activateContrato(ContratoId id) {
        Contrato contrato = contratoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato, id: " + id));
        if(contrato.isStatus()){
            throw new IllegalStateException("O contrato com ID: " + id + " já está ativado.");
        }
        contrato.setStatus(true);
        return contratoRepository.save(contrato);
    }
}