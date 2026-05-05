package com.ashencrown.service;

import com.ashencrown.dto.*;
import com.ashencrown.model.User;
import com.ashencrown.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository  = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponseDto register(RegisterRequestDto request) {
        if (userRepository.existsByUsername(request.getUsername()))
            return AuthResponseDto.error("Este nombre de usuario ya está en uso.");
        if (userRepository.existsByEmail(request.getEmail()))
            return AuthResponseDto.error("Este correo ya tiene una cuenta asociada.");
        if (request.getPassword().length() < 6)
            return AuthResponseDto.error("La contraseña debe tener al menos 6 caracteres.");

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);

        return AuthResponseDto.success(user.getId(), user.getUsername(),
                user.isHasSavedGame(), user.getLanguage(), "Cuenta creada exitosamente.");
    }

    public AuthResponseDto login(LoginRequestDto request) {
        User user = userRepository.findByUsername(request.getUsername()).orElse(null);
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash()))
            return AuthResponseDto.error("Nombre de usuario o contraseña incorrectos.");

        return AuthResponseDto.success(user.getId(), user.getUsername(),
                user.isHasSavedGame(), user.getLanguage(), "Sesión iniciada correctamente.");
    }

    public AuthResponseDto updateLanguage(Long userId, String language) {
        if (userId == null)
            return AuthResponseDto.error("ID de usuario inválido.");
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
        if (!language.equals("es") && !language.equals("en"))
            return AuthResponseDto.error("Idioma no válido.");
        user.setLanguage(language);
        userRepository.save(user);
        return AuthResponseDto.success(user.getId(), user.getUsername(),
                user.isHasSavedGame(), user.getLanguage(), "Idioma actualizado.");
    }
}
