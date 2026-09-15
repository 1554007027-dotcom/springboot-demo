package com.example.demo.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret",
                "my-super-secret-key-for-jwt-demo-please-change-in-production-1234567890");
        ReflectionTestUtils.setField(jwtUtil, "accessExpiration", 1800000L);
        ReflectionTestUtils.setField(jwtUtil, "refreshExpiration", 604800000L);
    }

    @Test
    void generateAccessToken_shouldHaveTypeAccess() {
        String token = jwtUtil.generateAccessToken("admin", "ADMIN");
        assertThat(token).isNotNull();
        assertThat(token.split("\\.")).hasSize(3);
        assertThat(jwtUtil.getTypeFromToken(token)).isEqualTo("access");
    }

    @Test
    void generateRefreshToken_shouldHaveTypeRefresh() {
        String token = jwtUtil.generateRefreshToken("admin", "ADMIN");
        assertThat(jwtUtil.getTypeFromToken(token)).isEqualTo("refresh");
    }

    @Test
    void accessToken_shouldContainUsernameAndRole() {
        String token = jwtUtil.generateAccessToken("admin", "ADMIN");
        assertThat(jwtUtil.getUsernameFromToken(token)).isEqualTo("admin");
        assertThat(jwtUtil.getRoleFromToken(token)).isEqualTo("ADMIN");
    }

    @Test
    void validateToken_shouldMatchType() {
        String accessToken = jwtUtil.generateAccessToken("admin", "ADMIN");
        String refreshToken = jwtUtil.generateRefreshToken("admin", "ADMIN");

        assertThat(jwtUtil.validateToken(accessToken, "access")).isTrue();
        assertThat(jwtUtil.validateToken(accessToken, "refresh")).isFalse();
        assertThat(jwtUtil.validateToken(refreshToken, "refresh")).isTrue();
        assertThat(jwtUtil.validateToken(refreshToken, "access")).isFalse();
    }

    @Test
    void validateToken_shouldReturnFalseForInvalidToken() {
        assertThat(jwtUtil.validateToken("invalid.token.here", "access")).isFalse();
        assertThat(jwtUtil.validateToken("", "access")).isFalse();
    }

    @Test
    void validateToken_shouldReturnFalseForTamperedToken() {
        String token = jwtUtil.generateAccessToken("admin", "ADMIN");
        String tampered = token.substring(0, token.length() - 5) + "xxxxx";
        assertThat(jwtUtil.validateToken(tampered, "access")).isFalse();
    }
}