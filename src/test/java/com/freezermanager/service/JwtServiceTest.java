package com.freezermanager.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

import org.junit.jupiter.api.Test;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.WeakKeyException;

class JwtServiceTest {

    private static final String VALID_SECRET =
            "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";

    private static final long VALID_EXPIRATION = 3600000L;

    @Test
    void shouldCreateJwtServiceWithValidConfiguration() {

        JwtService jwtService = new JwtService(
                VALID_SECRET,
                VALID_EXPIRATION
        );

        assertNotNull(jwtService);
    }

    @Test
    void shouldThrowExceptionWhenSecretIsNull() {

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> new JwtService(null, VALID_EXPIRATION)
        );

        assertEquals(
                "La configuración security.jwt.secret es obligatoria.",
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowExceptionWhenSecretIsEmpty() {

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> new JwtService("", VALID_EXPIRATION)
        );

        assertEquals(
                "La configuración security.jwt.secret es obligatoria.",
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowExceptionWhenSecretIsTooShort() {

        String shortSecret = "YWJj";

        assertThrows(
                WeakKeyException.class,
                () -> new JwtService(shortSecret, VALID_EXPIRATION)
        );
    }

    @Test
    void shouldThrowExceptionWhenExpirationIsZero() {

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> new JwtService(VALID_SECRET, 0)
        );

        assertEquals(
                "La expiración del JWT debe ser mayor que cero.",
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowExceptionWhenExpirationIsNegative() {

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> new JwtService(VALID_SECRET, -1000)
        );

        assertEquals(
                "La expiración del JWT debe ser mayor que cero.",
                exception.getMessage()
        );
    }

    @Test
    void shouldGenerateTokenWithCorrectEmail() {

        JwtService jwtService = new JwtService(
                VALID_SECRET,
                VALID_EXPIRATION
        );

        String email = "test@example.com";

        String token = jwtService.generateToken(email);

        assertNotNull(token);
        assertFalse(token.isBlank());
        assertEquals(email, jwtService.extractEmail(token));
    }

    @Test
    void shouldReturnTrueWhenTokenIsValidForExpectedEmail() {

        JwtService jwtService = new JwtService(
                VALID_SECRET,
                VALID_EXPIRATION
        );

        String email = "test@example.com";

        String token = jwtService.generateToken(email);

        boolean result = jwtService.isTokenValid(token, email);

        assertTrue(result);
    }

    @Test
    void shouldReturnFalseWhenTokenEmailDoesNotMatchExpectedEmail() {

        JwtService jwtService = new JwtService(
                VALID_SECRET,
                VALID_EXPIRATION
        );

        String token = jwtService.generateToken("test@example.com");

        boolean result = jwtService.isTokenValid(
                token,
                "another@example.com"
        );

        assertFalse(result);
    }

    @Test
    void shouldThrowExceptionWhenTokenIsTampered() {

        JwtService jwtService = new JwtService(
                VALID_SECRET,
                VALID_EXPIRATION
        );

        String token = jwtService.generateToken("test@example.com");

        String anotherToken = jwtService.generateToken("attacker@example.com");

        String[] originalParts = token.split("\\.");
        String[] anotherParts = anotherToken.split("\\.");

        String tamperedToken = originalParts[0]
                + "." + anotherParts[1]
                + "." + originalParts[2];

        assertThrows(
                JwtException.class,
                () -> jwtService.isTokenValid(
                        tamperedToken,
                        "test@example.com"
                )
        );
    }

    @Test
    void shouldThrowExceptionWhenTokenIsSignedWithDifferentSecret() {

        JwtService jwtService = new JwtService(
                VALID_SECRET,
                VALID_EXPIRATION
        );

        String anotherSecret =
                "YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXowMTIzNDU=";

        JwtService anotherJwtService = new JwtService(
                anotherSecret,
                VALID_EXPIRATION
        );

        String token = anotherJwtService.generateToken("test@example.com");

        assertThrows(
                JwtException.class,
                () -> jwtService.extractEmail(token)
        );
    }

    @Test
    void shouldThrowExceptionWhenTokenIsExpired() {

        JwtService jwtService = new JwtService(
                VALID_SECRET,
                VALID_EXPIRATION
        );

        var signingKey = Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(VALID_SECRET)
        );

        Date now = new Date();

        String expiredToken = Jwts.builder()
                .subject("test@example.com")
                .issuedAt(new Date(now.getTime() - 10000))
                .expiration(new Date(now.getTime() - 1000))
                .signWith(signingKey)
                .compact();

        assertThrows(
                ExpiredJwtException.class,
                () -> jwtService.isTokenValid(
                        expiredToken,
                        "test@example.com"
                )
        );
    }

    @Test
    void shouldThrowExceptionWhenSecretIsBlank() {

        assertThrows(
                IllegalStateException.class,
                () -> new JwtService("   ", VALID_EXPIRATION)
        );
    }

    @Test
    void shouldReturnFalseWhenExpectedEmailIsNull() {

        JwtService jwtService = new JwtService(
                VALID_SECRET,
                VALID_EXPIRATION
        );

        String token = jwtService.generateToken("test@example.com");

        boolean result = jwtService.isTokenValid(token, null);

        assertFalse(result);
    }
}