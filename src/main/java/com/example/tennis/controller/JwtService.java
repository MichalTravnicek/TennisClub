package com.example.tennis.controller;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

@Service
@Slf4j
public class JwtService {

    @Value("${security.jwt.secret}")
    private String secret;

    @Value("${security.jwt.expiration}")
    private long jwtExpiration;

    @Value("${security.jwt.refresh-expiration}")
    private long refreshExpiration;

    public SecretKey getSignInKey() {
        try {
            byte[] bytes = Base64.getDecoder().decode(secret);
            return Keys.hmacShaKeyFor(bytes);

        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Malformed secret");
        }
    }

    public String generateToken(UserDetails user) {
        return Jwts.builder()
                .subject(user.getUsername())
                .claim("role", user.getAuthorities().iterator().next().getAuthority())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey())
                .compact();
    }

    public String extractUsername(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(getSignInKey()).build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean isTokenValid(String token, UserDetails user) {
        try {
            //When token is expired JWT will throw exception
            return extractUsername(token).equals(user.getUsername());
        } catch (JwtException e) {
            log.debug("JWT exception:" + e);
            return false;
        }
    }
}
