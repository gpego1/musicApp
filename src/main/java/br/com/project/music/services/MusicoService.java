package br.com.project.music.services;

import br.com.project.music.business.dtos.MusicoDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface MusicoService {
    MusicoDTO createMusico(MusicoDTO musicoDTO);
    List<MusicoDTO> getAllMusicos();
    Optional<MusicoDTO> getMusicoById(Long id);
    MusicoDTO updateMusico(Long id, MusicoDTO musicoDTO);
    void deleteMusico(Long id);
}
