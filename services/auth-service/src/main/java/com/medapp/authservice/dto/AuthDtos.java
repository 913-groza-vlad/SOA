package com.medapp.authservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AuthDtos {
    public record RegisterRequest(
            @NotBlank @Size(min=3,max=150) String username,
            @NotBlank @Email String email,
            @NotBlank @Size(min=6,max=120) String password,
            @NotBlank String role
    ) {}
    public record LoginRequest(
            @NotBlank String username,
            @NotBlank String password
    ) {}
    public record TokenResponse(String accessToken, String tokenType) {}
}
