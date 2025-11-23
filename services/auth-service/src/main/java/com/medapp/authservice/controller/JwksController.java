package com.medapp.authservice.controller;

import com.medapp.authservice.security.KeyManager;
import com.nimbusds.jose.jwk.JWKSet;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class JwksController {
    private final KeyManager keys;
    public JwksController(KeyManager keys) { this.keys = keys; }

    @GetMapping(value="/.well-known/jwks.json", produces=MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> jwks() {
        JWKSet set = keys.getJwkSet();
        return set.toJSONObject();
    }
}
