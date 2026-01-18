package com.example.bankcards.config;

import com.example.bankcards.entity.enums.Role;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class JwtService {

    @Value("${spring.security.jwt.secret}")
    private String secret;

    private static final long EXPIRATION_MS = 60 * 60 * 1000;

    public String generateToken(UUID userId, String username, List<Role> roles) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + EXPIRATION_MS);

        List<String> roleNames = roles.stream()
                .map(Role::name)
                .toList();

        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId.toString())
                .claim("roles", roleNames)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(SignatureAlgorithm.HS256, secret.getBytes(StandardCharsets.UTF_8))
                .compact();
    }
}