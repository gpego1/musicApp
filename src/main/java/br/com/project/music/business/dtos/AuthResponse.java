package br.com.project.music.business.dtos;

import br.com.project.music.business.entities.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private Long userId;
    private String email;
    private String name;
    private User.Role role;
    private String errorMessage;

    public AuthResponse(String token, Long userId, String name, String email, User.Role role) {
        this.token = token;
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
    }
    public AuthResponse(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
