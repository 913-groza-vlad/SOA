package com.medapp.authservice.service;

import com.medapp.authservice.domain.Role;
import com.medapp.authservice.domain.UserAccount;
import com.medapp.authservice.repo.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserAccountRepository repo;
    private final PasswordEncoder encoder;
    private final TokenService tokens;

    public void register(String username, String email, String rawPassword, Role role) {
        if (repo.existsByUsername(username)) throw new IllegalArgumentException("username_taken");
        if (repo.existsByEmail(email)) throw new IllegalArgumentException("email_taken");
        UserAccount ua = UserAccount.builder()
                .username(username)
                .email(email)
                .passwordHash(encoder.encode(rawPassword))
                .role(role)
                .build();
        repo.save(ua);
    }

    public String login(String username, String rawPassword) {
        UserAccount ua = repo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("invalid_credentials"));
        if (!encoder.matches(rawPassword, ua.getPasswordHash())) {
            throw new IllegalArgumentException("invalid_credentials");
        }
        return tokens.createToken(ua);
    }
}
