package br.com.project.music.services.impl;

import br.com.project.music.business.dtos.Auth;
import br.com.project.music.business.entities.User;
import br.com.project.music.services.AuthService;
import br.com.project.music.services.UserService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Autowired
    private UserService userService;

    @Override
    public String authenticate(Auth auth) {
        User user = userService.getUserByEmailAndSenha(auth.getEmail(), auth.getSenha());
        if (user == null) {
            Optional<User> userOptional = userService.getUserByEmail(auth.getEmail());
            if(userOptional.isPresent()){
                user = userOptional.get();
            }
        }
        if (user != null) {
            logger.info("Gerando token JWT para o usuário: {}", user.getEmail());
            return Jwts.builder()
                    .setSubject(user.getEmail())
                    .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                    .signWith(SignatureAlgorithm.HS256, jwtSecret)
                    .compact();
        }
        return null;
    }
}