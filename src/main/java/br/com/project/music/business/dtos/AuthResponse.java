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
    private String photo;
    private Boolean isGoogleUser;
    private Boolean hasPassword;
    private String errorMessage;

    public AuthResponse(String token, Long userId, String name, String email, User.Role role, String photo, Boolean isGoogleUser, Boolean hasPassword) {
        this.token = token;
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
        this.photo = photo;
        this.isGoogleUser = isGoogleUser;
        this.hasPassword = hasPassword;
    }
    public AuthResponse(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
