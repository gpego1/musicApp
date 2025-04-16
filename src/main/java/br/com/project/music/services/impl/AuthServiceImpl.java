package br.com.project.music.services.impl;

import br.com.project.music.business.dtos.Auth;
import br.com.project.music.services.AuthService;
import br.com.project.music.services.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);
    private static final long EXPIRATION_TIME = 86400000; // 24 horas

    @Value("${jwt.secret}")
    private String jwtSecret;

    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    public AuthServiceImpl(UserService userService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    @Override
    public String authenticate(Auth auth) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            auth.getEmail(),
                            auth.getSenha()
                    )
            );

            // Gera o token JWT
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            return generateToken(userDetails);

        } catch (Exception e) {
            logger.error("Falha na autenticação para o email: {}", auth.getEmail(), e);
            return null;
        }
    }

    private String generateToken(UserDetails userDetails) {
        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .claim("roles", userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parseToken(String token) {
        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        return Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}