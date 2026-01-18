package com.example.bankcards.controller.external;

import com.example.bankcards.config.JwtService;
import com.example.bankcards.entity.enums.Role;
import com.example.bankcards.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserService userService;

    public AuthController(JwtService jwtService,
                          AuthenticationManager authenticationManager,
                          UserService userService) {
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.userService = userService;
    }

    /**
     * Логин:
     * 1) проверяем username/password через AuthenticationManager
     * 2) достаем роли из Authentication (authorities)
     * 3) достаем userId по username
     * 4) генерируем JWT, где claim "roles" и "userId"
     */
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );
        } catch (BadCredentialsException ex) {
            throw new UnauthorizedException("Invalid username or password");
        }

        String username = authentication.getName();

        List<Role> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> a.substring("ROLE_".length()))
                .map(String::toUpperCase)
                .map(Role::valueOf)
                .toList();

        UUID userId = userService.getIdByUsername(username);

        String token = jwtService.generateToken(userId, username, roles);
        return new LoginResponse(token, "Bearer");
    }

    /**
     * "Кто я?" — удобно для фронта
     */
    @GetMapping("/me")
    public MeResponse me(@AuthenticationPrincipal Jwt jwt) {
        return new MeResponse(
                jwt.getClaimAsString("userId"),
                jwt.getSubject(),
                jwt.getClaim("roles")
        );
    }

    public record LoginRequest(
            @NotBlank String username,
            @NotBlank String password
    ) {}

    public record LoginResponse(String token, String tokenType) {}

    public record MeResponse(String userId, String username, Object roles) {}

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public static class UnauthorizedException extends RuntimeException {
        public UnauthorizedException(String message) {
            super(message);
        }
    }
}
