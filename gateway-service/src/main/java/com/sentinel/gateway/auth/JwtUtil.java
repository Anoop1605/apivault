package com.sentinel.gateway.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

/**
 * Utility class for RS256 JWT parsing and validation.
 * Loads the public key from a configurable file path.
 * Validates signature, expiry, issuer, and audience per PRD FR-AU-02.
 */
@Slf4j
@Component
public class JwtUtil {

    @Value("${sentinel.jwt.public-key-path:src/main/resources/keys/public_key.pem}")
    private String publicKeyPath;

    @Value("${sentinel.jwt.issuer:sentinel}")
    private String expectedIssuer;

    @Value("${sentinel.jwt.audience:sentinel-api}")
    private String expectedAudience;

    private PublicKey publicKey;

    @PostConstruct
    public void init() {
        try {
            String keyContent = Files.readString(Path.of(publicKeyPath));
            String keyPem = keyContent
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s+", "");
            byte[] keyBytes = Base64.getDecoder().decode(keyPem);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory factory = KeyFactory.getInstance("RSA");
            this.publicKey = factory.generatePublic(spec);
            log.info("RS256 public key loaded successfully from: {}", publicKeyPath);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            log.error("Failed to load RS256 public key from path: {}", publicKeyPath, e);
            throw new RuntimeException("Cannot start gateway without valid RS256 public key", e);
        }
    }

    /**
     * Parse and validate the JWT token.
     * Verifies RS256 signature, expiry, issuer, and audience.
     *
     * @param token the JWT token string (without "Bearer " prefix)
     * @return parsed Claims if valid
     * @throws JwtException if validation fails
     */
    public Claims parseAndValidate(String token) {
        return Jwts.parser()
                .verifyWith(publicKey)
                .requireIssuer(expectedIssuer)
                .requireAudience(expectedAudience)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extract user_id (subject) from claims.
     */
    public String getUserId(Claims claims) {
        return claims.getSubject();
    }

    /**
     * Extract roles from JWT claims.
     */
    @SuppressWarnings("unchecked")
    public List<String> getRoles(Claims claims) {
        Object rolesObj = claims.get("roles");
        if (rolesObj instanceof List) {
            return (List<String>) rolesObj;
        }
        return Collections.emptyList();
    }

    /**
     * Extract scope from JWT claims.
     */
    public String getScope(Claims claims) {
        return claims.get("scope", String.class);
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }
}
