package nl.inholland.codegen.bankingapp.utils;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {
    private static final String SECRET = "wNGVp8g65zeYvMprbMROoT2oZJVvpVq76fakQTPYOkNpmojQN6+ZoLywZg==";
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    private static final long EXPIRATION_MS = 1000 * 60 * 60;

    public static final String BEARER_PREFIX = "Bearer ";

    public String generateToken(String username) {
        long currentTime = System.currentTimeMillis();
        
        Date iat = new Date(currentTime);
        Date eat = new Date(currentTime + EXPIRATION_MS);

        return Jwts
            .builder()
            .subject(username)
            .issuedAt(iat)
            .expiration(eat)
            .signWith(SECRET_KEY)
            .compact();
    }

    public String extractUsername(String token) {
        String jwt = token != null && token.startsWith(BEARER_PREFIX)
            ? token.substring(BEARER_PREFIX.length()) // is a bearer token, we strip the prefix
            : token; // not a bearer token

        return Jwts.parser()
            .verifyWith(SECRET_KEY)
            .build()
            .parseSignedClaims(jwt)
            .getPayload()
            .getSubject();
    }
}
