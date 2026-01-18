package com.example.bankcards.controller.external;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class AdminController {

    /**
     * Минимальный эндпоинт для проверки:
     * - если токен ADMIN -> 200 OK
     * - если токен USER -> 403 Forbidden
     * - если без токена -> 401 Unauthorized
     */
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("ADMIN OK ✅");
    }
}