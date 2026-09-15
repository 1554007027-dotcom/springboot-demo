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
        // 手动注入 @Value 字段（因为没有 Spring 容器）
        ReflectionTestUtils.setField(jwtUtil, "secret",
                "my-super-secret-key-for-jwt-demo-please-change-in-production-1234567890");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 86400000L);
    }

    @Test
    void generateToken_shouldReturnNonNullToken() {
        String token = jwtUtil.generateToken("admin", "ADMIN");
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        // JWT 是三段用 . 分隔
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void generateToken_shouldContainUsername() {
        String token = jwtUtil.generateToken("admin", "ADMIN");
        String username = jwtUtil.getUsernameFromToken(token);
        assertThat(username).isEqualTo("admin");
    }

    @Test
    void generateToken_shouldContainRole() {
        String token = jwtUtil.generateToken("admin", "ADMIN");
        String role = jwtUtil.getRoleFromToken(token);
        assertThat(role).isEqualTo("ADMIN");
    }

    @Test
    void validateToken_shouldReturnTrueForValidToken() {
        String token = jwtUtil.generateToken("admin", "ADMIN");
        assertThat(jwtUtil.validateToken(token)).isTrue();
    }

    @Test
    void validateToken_shouldReturnFalseForInvalidToken() {
        assertThat(jwtUtil.validateToken("invalid.token.here")).isFalse();
        assertThat(jwtUtil.validateToken("")).isFalse();
    }

    @Test
    void validateToken_shouldReturnFalseForTamperedToken() {
        String token = jwtUtil.generateToken("admin", "ADMIN");
        String tampered = token.substring(0, token.length() - 5) + "xxxxx";
        assertThat(jwtUtil.validateToken(tampered)).isFalse();
    }
}