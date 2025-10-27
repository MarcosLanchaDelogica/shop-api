package com.example.shop.security;

import com.example.shop.exceptions.UnauthorizedException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/**
 * Utilidad para generación y validación de tokens JWT.
 */
@Component
public class JwtTokenUtil {

    private static final String SECRET_KEY = "clave-secreta-muy-larga-para-jwt";
    private static final long EXPIRATION_TIME = 86400000; // 24h

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (ExpiredJwtException e) {
            throw new UnauthorizedException("El token ha expirado");
        } catch (MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            throw new UnauthorizedException("El token JWT no es válido");
        } catch (SecurityException e) {
            throw new UnauthorizedException("La firma del token JWT no es válida");
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            // Si no lanza excepción, el token es válido
            return true;
        } catch (JwtException e) {
            // Cualquier excepción JWT se traduce en no autorizado
            throw new UnauthorizedException("Token JWT inválido o expirado");
        }
    }

}
