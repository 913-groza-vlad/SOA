package com.medapp.authservice.security;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.*;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;

@Component
public class KeyManager {
    @Getter
    private final RSAKey rsaJwk;
    @Getter
    private final JWKSet jwkSet;

    public KeyManager() {
        try {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
            gen.initialize(2048);
            KeyPair kp = gen.generateKeyPair();
            RSAPublicKey pub = (RSAPublicKey) kp.getPublic();
            RSAPrivateKey priv = (RSAPrivateKey) kp.getPrivate();

            String kid = UUID.randomUUID().toString();
            this.rsaJwk = new RSAKey.Builder(pub)
                    .privateKey(priv)
                    .keyUse(KeyUse.SIGNATURE)
                    .algorithm(JWSAlgorithm.RS256)
                    .keyID(kid)
                    .build();

            this.jwkSet = new JWKSet(rsaJwk.toPublicJWK());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to init RSA keys", e);
        }
    }
}