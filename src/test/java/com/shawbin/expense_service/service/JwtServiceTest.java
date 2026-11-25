
package com.shawbin.expense_service.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private JwtService jwtService;

    private static final String SECRET =
            "MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=";

    private SecretKey signingKey;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        ReflectionTestUtils.setField(jwtService, "secret", SECRET);

        signingKey = Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(SECRET));
    }

    private String generateToken(String subject) {
        return Jwts.builder()
                .subject(subject)
                .expiration(Date.from(
                        Instant.now().plusSeconds(600)))
                .signWith(signingKey)
                .compact();
    }

    @Test
    void extractUserId_withValidToken_shouldReturnUserId() {
        String token = generateToken("1");

        Long result = jwtService.extractUserId(token);

        assertThat(result).isEqualTo(1L);
    }

    @Test
    void extractUserId_withDifferentUser_shouldReturnCorrectUserId() {
        String token = generateToken("25");

        Long result = jwtService.extractUserId(token);

        assertThat(result).isEqualTo(25L);
    }

    @Test
    void extractUserId_withExpiredToken_shouldThrowException() {
        String token = Jwts.builder()
                .subject("1")
                .expiration(Date.from(
                        Instant.now().minusSeconds(60)))
                .signWith(signingKey)
                .compact();

        assertThatThrownBy(() ->
                jwtService.extractUserId(token))
                .isInstanceOf(Exception.class);
    }

    @Test
    void extractUserId_withInvalidToken_shouldThrowException() {
        assertThatThrownBy(() ->
                jwtService.extractUserId("invalid-token"))
                .isInstanceOf(Exception.class);
    }

    @Test
    void extractUserId_withWrongSigningKey_shouldThrowException() {
        SecretKey wrongKey = Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(
                        "QUJDREVGR0hJSktMTU5PUFFSU1RVVldYWVphYmNkZWY="
                ));

        String token = Jwts.builder()
                .subject("1")
                .signWith(wrongKey)
                .compact();

        assertThatThrownBy(() ->
                jwtService.extractUserId(token))
                .isInstanceOf(Exception.class);
    }

    @Test
    void extractUserId_withNonNumericSubject_shouldThrowException() {
        String token = generateToken("abc");

        assertThatThrownBy(() ->
                jwtService.extractUserId(token))
                .isInstanceOf(NumberFormatException.class);
    }
}
