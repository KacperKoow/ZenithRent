package com.kacperkoow.zenithrent.backend.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        userDetails = new User("john.doe@example.com", "password", Collections.emptyList());
    }

    @Nested
    @DisplayName("Tests for token generation")
    class TokenGenerationTests {

        @Test
        @DisplayName("Should generate valid JWT token for user details")
        void shouldGenerateTokenForUserDetails() {
            String token = jwtService.generateToken(userDetails);

            assertThat(token).isNotNull();
            assertThat(token).isNotEmpty();
            assertThat(token.split("\\.")).hasSize(3);
        }

        @Test
        @DisplayName("Should generate token with extra claims")
        void shouldGenerateTokenWithExtraClaims() {
            Map<String, Object> extraClaims = Map.of("role", "ROLE_USER", "userId", 123L);

            String token = jwtService.generateToken(extraClaims, userDetails);

            assertThat(token).isNotNull();
            assertThat(token).isNotEmpty();
            assertThat(jwtService.extractUsername(token)).isEqualTo("john.doe@example.com");
        }
    }

    @Nested
    @DisplayName("Tests for claims extraction and validation")
    class ClaimsAndValidationTests {

        @Test
        @DisplayName("Should extract correct username from token")
        void shouldExtractUsernameFromToken() {
            String token = jwtService.generateToken(userDetails);

            String extractedUsername = jwtService.extractUsername(token);

            assertThat(extractedUsername).isEqualTo("john.doe@example.com");
        }

        @Test
        @DisplayName("Should validate token successfully for matching user details")
        void shouldValidateTokenSuccessfully() {
            String token = jwtService.generateToken(userDetails);

            boolean isValid = jwtService.isTokenValid(token, userDetails);

            assertThat(isValid).isTrue();
        }

        @Test
        @DisplayName("Should invalidate token when username does not match user details")
        void shouldInvalidateTokenWhenUsernameMismatches() {
            String token = jwtService.generateToken(userDetails);
            UserDetails differentUser = new User("other.user@example.com", "password", Collections.emptyList());

            boolean isValid = jwtService.isTokenValid(token, differentUser);

            assertThat(isValid).isFalse();
        }
    }
}