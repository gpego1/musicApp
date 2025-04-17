package br.com.project.music.business.dtos;

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
    private String userType;
    private String errorMessage;

    public AuthResponse(String token, Long userId, String name, String email, String userType) {
        this.token = token;
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.userType = userType;
    }
    public AuthResponse(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
