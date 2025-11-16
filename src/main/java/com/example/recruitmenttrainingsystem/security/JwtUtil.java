package com.example.recruitmenttrainingsystem.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private final Key key;
    private final long expirationMs = 1000L * 60 * 60 * 24; // 24h

    // ✅ KHÔNG dùng @Value nữa, chỉ dùng Environment
    public JwtUtil(Environment env) {
        // Lấy từ application.properties, nếu không có thì dùng fallback (cho dev)
        String secret = env.getProperty(
                "jwt.secret",
                "ThisIsAFallbackJwtSecretKey_ChangeMe_12345678901234567890"
        );

        System.out.println("🔐 JwtUtil init, secret length = " + secret.length());

        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String email, String role) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
