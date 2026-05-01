package nl.inholland.codegen.bankingapp.security;

import io.jsonwebtoken.*;
import nl.inholland.codegen.bankingapp.models.User;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class JwtService {

    private final String SECRET = "bXlTdXBlclNlY3JldEtleTEyMzQ1Njc4OTAxMjM0NTY3ODkw";

    public String generateToken(User user) {
        return Jwts.builder()
            .setSubject(user.getEmail())
            .claim("role", user.getRole().name())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
            .signWith(SignatureAlgorithm.HS256, SECRET)
            .compact();
    }

    public String extractEmail(String token) {
        return Jwts.parser()
            .setSigningKey(SECRET)
            .parseClaimsJws(token)
            .getBody()
            .getSubject();
    }
}
