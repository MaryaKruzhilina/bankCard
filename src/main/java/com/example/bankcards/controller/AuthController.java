package com.example.bankcards.controller;

import com.example.bankcards.entity.enums.Role;
import com.example.bankcards.config.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthController(JwtService jwtService,
                          AuthenticationManager authenticationManager) {
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    /**
     * Логин:
     * 1) проверяем username/password через AuthenticationManager
     * 2) достаем роли из Authentication (authorities)
     * 3) генерируем JWT, где claim "roles" = ["ADMIN","USER"...]
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest request) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );
        } catch (BadCredentialsException ex) {
            // Можно отдать 401 через @ControllerAdvice, но тут оставлю явно.
            return ResponseEntity.status(401).body(Map.of("error", "Invalid username or password"));
        }

        authentication.getAuthorities()
                .forEach(a -> System.out.println("AUTH: " + a.getAuthority()));

        // Превращаем authorities -> List<Role>
        // Ожидаем "ROLE_ADMIN", "ROLE_USER", ...
        List<Role> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> a.startsWith("ROLE_") ? a.substring("ROLE_".length()) : a)
                .map(String::toUpperCase)
                .map(Role::valueOf)
                .toList();

        String token = jwtService.generateToken(authentication.getName(), roles);

        return ResponseEntity.ok(Map.of(
                "token", token,
                "tokenType", "Bearer"
        ));
    }

    public record LoginRequest(
            String username,
            String password
    ) {}
}
