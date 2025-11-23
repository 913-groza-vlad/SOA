package com.medapp.authservice.controller;

import com.medapp.authservice.service.AuthService;

import com.medapp.authservice.domain.Role;
import com.medapp.authservice.dto.AuthDtos.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService app;

    public AuthController(AuthService app) { this.app = app; }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterRequest req) {
        app.register(req.username(), req.email(), req.password(), Role.valueOf(req.role().toUpperCase()));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody @Valid LoginRequest req) {
        String token = app.login(req.username(), req.password());
        return ResponseEntity.ok(new TokenResponse(token, "Bearer"));
    }
}
