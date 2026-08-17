package com.example.eventplatform.auth;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final JwtService jwtService;
    private final String demoUser;
    private final String demoPassword;
    public AuthController(JwtService jwtService,
                          @Value("${app.security.demo-user:admin}") String demoUser,
                          @Value("${app.security.demo-password:admin123}") String demoPassword) {
        this.jwtService = jwtService;
        this.demoUser = demoUser;
        this.demoPassword = demoPassword;
    }
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        if (!demoUser.equals(request.username()) || !demoPassword.equals(request.password())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        return new LoginResponse(jwtService.createToken(request.username()), "Bearer");
    }
}
