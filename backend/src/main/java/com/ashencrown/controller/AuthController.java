package com.ashencrown.controller;

import com.ashencrown.dto.*;
import com.ashencrown.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints de autenticación.
 * Base URL: http://localhost:8080/api/auth
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // POST /api/auth/register — crear cuenta
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register(@RequestBody RegisterRequestDto request) {
        AuthResponseDto response = authService.register(request);
        return response.isSuccess()
                ? ResponseEntity.ok(response)
                : ResponseEntity.badRequest().body(response);
    }

    // POST /api/auth/login — iniciar sesión
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@RequestBody LoginRequestDto request) {
        AuthResponseDto response = authService.login(request);
        return response.isSuccess()
                ? ResponseEntity.ok(response)
                : ResponseEntity.badRequest().body(response);
    }

    // PUT /api/auth/language/{userId} — cambiar idioma
    @PutMapping("/language/{userId}")
    public ResponseEntity<AuthResponseDto> updateLanguage(
            @PathVariable Long userId,
            @RequestBody LanguageRequestDto request) {
        AuthResponseDto response = authService.updateLanguage(userId, request.getLanguage());
        return response.isSuccess()
                ? ResponseEntity.ok(response)
                : ResponseEntity.badRequest().body(response);
    }
}
