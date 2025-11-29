package com.medapp.authservice.service;

import com.medapp.authservice.domain.Role;
import com.medapp.authservice.domain.UserAccount;
import com.medapp.authservice.security.KeyManager;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;

@Service
public class TokenService {
    private final KeyManager keys;
    private final String issuer;
    private final long ttlMinutes;

    public TokenService(
            KeyManager keys,
            @Value("${auth.issuer}") String issuer,
            @Value("${auth.ttl-minutes}") long ttlMinutes) {
        this.keys = keys;
        this.issuer = issuer;
        this.ttlMinutes = ttlMinutes;
    }

    public String createToken(UserAccount user) {
        try {
            Instant now = Instant.now();
            Instant exp = now.plusSeconds(ttlMinutes * 60);

            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(user.getUsername())
                    .issuer(issuer)
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(exp))
                    .claim("role", user.getRole().name())
                    .claim("userId", user.getId())
                    .build();

            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                    .keyID(keys.getRsaJwk().getKeyID())
                    .type(JOSEObjectType.JWT)
                    .build();

            SignedJWT jwt = new SignedJWT(header, claims);
            JWSSigner signer = new RSASSASigner(keys.getRsaJwk().toPrivateKey());
            jwt.sign(signer);
            return jwt.serialize();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to sign JWT", e);
        }
    }
}
