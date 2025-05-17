package br.com.project.music.services;

import br.com.project.music.business.dtos.Auth;
import br.com.project.music.business.dtos.GoogleUserInfo;
import br.com.project.music.business.entities.User;

import java.util.Optional;

public interface AuthService {
    String authenticate(Auth auth);
    String generateTokenForGoogle(String email);
    String getEmailFromToken(String token);
    GoogleUserInfo verifyGoogleToken(String idToken);
    String authenticateWithGoogle(String googleToken);
}
