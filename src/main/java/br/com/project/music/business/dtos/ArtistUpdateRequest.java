package br.com.project.music.business.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ArtistUpdateRequest {
    @NotBlank
    private String role;

    @NotBlank
    private String nome_artistico;

    private String redes_sociais;

    @NotNull
    private Long id_usuario;
}
