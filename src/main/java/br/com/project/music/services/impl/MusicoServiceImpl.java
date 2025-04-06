package br.com.project.music.services.impl;
import br.com.project.music.business.dtos.MusicoDTO;
import br.com.project.music.business.entities.Musico;
import br.com.project.music.business.repositories.MusicoRepository;
import br.com.project.music.services.MusicoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MusicoServiceImpl implements MusicoService {

    @Autowired
    private MusicoRepository musicoRepository;

    @Override
    @Transactional
    public MusicoDTO createMusico(MusicoDTO musicoDTO) {
        Musico musico = new Musico();
        musico.setUsuario(musicoDTO.getUsuario());
        musico.setNomeArtistico(musicoDTO.getNomeArtistico());
        musico.setRedesSociais(musicoDTO.getRedesSociais());
        Musico savedMusico = musicoRepository.save(musico);
        return convertToDTO(savedMusico);
    }
    @Override
    public List<MusicoDTO> getAllMusicos() {
        return musicoRepository.findAll().stream()
               .map(this::convertToDTO)
               .collect(Collectors.toList());
    }
    @Override
    public Optional<MusicoDTO> getMusicoById(Long id) {
        return musicoRepository.findByIdMusico(id).map(this::convertToDTO);
    }
    @Override
    @Transactional
    public MusicoDTO updateMusico(Long id, MusicoDTO musicoDTO) {
        Musico musico = musicoRepository.findById(id).orElse(null);
        if (musico != null) {
            musico.setUsuario(musicoDTO.getUsuario());
            musico.setNomeArtistico(musicoDTO.getNomeArtistico());
            musico.setRedesSociais(musicoDTO.getRedesSociais());
            Musico savedMusico = musicoRepository.save(musico);
            return convertToDTO(savedMusico);
        }
        return null;
    }
    @Override
    @Transactional
    public void deleteMusico(Long id) {
        musicoRepository.deleteById(id);
    }
    private MusicoDTO convertToDTO(Musico musico) {
        return new MusicoDTO(
                musico.getIdMusico(),
                musico.getUsuario(),
                musico.getNomeArtistico(),
                musico.getRedesSociais()
        );
    }
}
