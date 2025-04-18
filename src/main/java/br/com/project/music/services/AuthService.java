package br.com.project.music.services;

import br.com.project.music.business.dtos.Auth;

public interface AuthService {
    String authenticate(Auth auth);
    String generateTokenForGoogle(String email);
    String getEmailFromToken(String token);
}
