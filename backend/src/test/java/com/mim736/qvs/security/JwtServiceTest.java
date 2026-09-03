package com.mim736.qvs.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private final JwtService jwtService = new JwtService("unit-test-secret-key-32-bytes-ok", 3_600_000);

    @Test
    void roundTripUsername() {
        String token = jwtService.generateToken("issuer", "ISSUER");
        assertEquals("issuer", jwtService.extractUsername(token));
        assertTrue(jwtService.isValid(token));
    }

    @Test
    void tamperedTokenIsRejected() {
        String token = jwtService.generateToken("issuer", "ISSUER");
        assertFalse(jwtService.isValid(token + "x"));
    }
}
