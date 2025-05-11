package br.com.project.music.services.impl;

import br.com.project.music.business.dtos.Auth;
import br.com.project.music.business.dtos.GoogleUserInfo;
import br.com.project.music.business.entities.User;
import br.com.project.music.services.AuthService;
import br.com.project.music.services.UserService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Autowired
    private UserService userService;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public String authenticate(Auth auth) {
        User user = userService.getUserByEmailAndSenha(auth.getEmail(), auth.getSenha());
        if (user == null) {
            Optional<User> userOptional = userService.getUserByEmail(auth.getEmail());
            if (userOptional.isPresent()) {
                user = userOptional.get();
            }
        }
        if (user != null) {
            logger.info("Gerando token JWT para o usuário: {}", user.getEmail());
            return Jwts.builder()
                    .setSubject(user.getEmail())
                    .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                    .signWith(getSigningKey()) // Use getSigningKey()
                    .compact();
        }
        return null;
    }

    @Override
    public String generateTokenForGoogle(String email) {
        logger.info("Gerando token JWT para login com Google do usuário: {}", email);
        return Jwts.builder()
                .setSubject(email)
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(getSigningKey()) // Use getSigningKey()
                .compact();
    }
    public GoogleUserInfo verifyGoogleToken(String idToken) {
        if (idToken == null || idToken.length() < 100) {
            throw new IllegalArgumentException("Invalid token format");
        }
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    new GsonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();
            GoogleIdToken googleIdToken = verifier.verify(idToken);
            if (googleIdToken != null) {
                GoogleIdToken.Payload payload = googleIdToken.getPayload();

                return new GoogleUserInfo(
                        payload.getSubject(),
                        payload.getEmail(),
                        (String) payload.get("name"),
                        (String) payload.get("picture"));
            }
            throw new RuntimeException("Token do Google inválido");
        } catch (Exception e) {
            throw new RuntimeException("Falha na verificação do token do Google: " + e.getMessage());
        }
    }
    public String authenticateWithGoogle(String googleToken) {
        GoogleUserInfo googleUser = verifyGoogleToken(googleToken);
        return generateTokenForGoogle(googleUser.getEmail());
    }


    public String getEmailFromToken(String token) {
        try {
            Claims claims = Jwts.parser() // Use parserBuilder()
                    .setSigningKey(getSigningKey()) // Set the signing key
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getSubject();
        } catch (JwtException e) {
            logger.error("Erro ao obter email do token", e);
            return null;
        }
    }

}