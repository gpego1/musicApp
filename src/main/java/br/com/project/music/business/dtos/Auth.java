package br.com.project.music.business.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class Auth {
    @NotBlank(message = "Email é obrigatório")
    private String email;
    @NotBlank(message = "Senha é obrigatória")
    private String senha;
}
