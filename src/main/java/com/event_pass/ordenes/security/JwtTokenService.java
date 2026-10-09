package com.event_pass.ordenes.security;

import java.util.Base64;
import java.util.Objects;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtTokenService {

    private final SecretKey signingKey;
    private final String issuer;

    public JwtTokenService(
        @Value("${app.jwt.secret-base64}") String secretBase64,
        @Value("${app.jwt.issuer}") String issuer
    ) {
        this.signingKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secretBase64));
        this.issuer = issuer;
    }

    public String validarComprador(String authorization, Long usuarioIdSolicitado) {
        if (authorization == null || !authorization.startsWith("Bearer ")
            || authorization.substring(7).isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Se requiere un token Bearer valido");
        }

        String token = authorization.substring(7).trim();
        Long usuarioIdAutenticado;
        String rol;
        try {
            Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(token)
                .getPayload();
            usuarioIdAutenticado = Long.valueOf(claims.getSubject());
            rol = claims.get("rol", String.class);
        } catch (JwtException | IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "El token no es valido o ha expirado");
        }

        if (!"COMPRADOR".equals(rol)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo un comprador puede crear una orden");
        }

        if (!Objects.equals(usuarioIdAutenticado, usuarioIdSolicitado)) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "El usuario de la solicitud no coincide con el usuario autenticado"
            );
        }

        return "Bearer " + token;
    }
}
