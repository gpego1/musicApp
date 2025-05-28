package br.com.project.music.business.dtos;

import br.com.project.music.business.entities.Escala;
import br.com.project.music.business.entities.Musico;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class EscalaResponseDTO {
    private Long idEvento;
    private Long idGeneroMusical;
    private String nomeEvento;
    private String nomeGenero;
    private List<MusicoResponseDTO> musicos;

    public EscalaResponseDTO(Escala escala) {
        this.idEvento = escala.getIdEscala().getEvento().getIdEvento();
        this.idGeneroMusical = escala.getIdEscala().getGenero().getIdGeneroMusical();
        this.nomeEvento = escala.getIdEscala().getEvento().getNomeEvento();
        this.nomeGenero = escala.getIdEscala().getGenero().getNomeGenero();
        this.musicos = escala.getMusicos().stream()
                .map(MusicoResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Data
    public static class MusicoResponseDTO {
        private Long idMusico;
        private String nomeMusico;

        public MusicoResponseDTO(Musico musico) {
            this.idMusico = musico.getIdMusico();
            this.nomeMusico = musico.getNomeArtistico();
        }
    }
}