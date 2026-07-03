package com.example.eventplatform.controller;

import com.example.eventplatform.dto.*;
import com.example.eventplatform.security.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final JwtService jwtService;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        if (!"admin".equals(request.username()) || !"admin123".equals(request.password())) {
            throw new IllegalArgumentException("Invalid username or password");
        }
        return new LoginResponse(jwtService.generateToken(request.username()), "Bearer");
    }
}
