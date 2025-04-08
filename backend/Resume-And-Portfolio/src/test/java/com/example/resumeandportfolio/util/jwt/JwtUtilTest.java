package com.example.resumeandportfolio.util.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * JWT Utility Test
 *
 * @author Ji-Seungwoo
 * @version 1.0
 */

class JwtUtilTest {

    private JwtUtil jwtUtil;

    private final String secretKey = "mySuperSecretKeyForJwtTesting12345";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(secretKey);
    }

    @Test
    @DisplayName("JWT 생성 테스트")
    void createJwtTest() {
        // Given
        String category = "access";
        String username = "test@example.com";
        String role = "ROLE_VISITOR";
        String nickname = "test";
        long expiredMs = 60000L; // 1 minute

        // When
        String token = jwtUtil.createJwt(category, username, role, nickname, expiredMs);

        // Then
        assertThat(token).isNotNull();
    }

    @Test
    @DisplayName("JWT 파싱 테스트 - 카테고리, 사용자 이름, 역할")
    void parseJwtTest() {
        // Given
        String category = "access";
        String username = "test@example.com";
        String role = "ROLE_VISITOR";
        String nickname = "test";
        long expiredMs = 60000L; // 1 minute
        String token = jwtUtil.createJwt(category, username, role, nickname, expiredMs);

        // When
        String parsedCategory = jwtUtil.getCategory(token);
        String parsedUsername = jwtUtil.getUsername(token);
        String parsedRole = jwtUtil.getRole(token);

        // Then
        assertThat(parsedCategory).isEqualTo(category);
        assertThat(parsedUsername).isEqualTo(username);
        assertThat(parsedRole).isEqualTo(role);
    }

    @Test
    @DisplayName("JWT 만료 확인 테스트 - 유효한 토큰")
    void isNotExpiredTest() {
        // Given
        String token = jwtUtil.createJwt("access", "test@example.com", "ROLE_VISITOR", "test",
            60000L);

        // When
        boolean isExpired = jwtUtil.isExpired(token);

        // Then
        assertThat(isExpired).isFalse();
    }

    @Test
    @DisplayName("JWT 만료 확인 테스트 - 만료된 토큰")
    void isExpiredTest() throws InterruptedException {
        // Given
        String token = jwtUtil.createJwt("access", "test@example.com", "ROLE_VISITOR", "test",
            1000L);

        Thread.sleep(2000L);

        // When
        boolean isExpired;
        try {
            isExpired = jwtUtil.isExpired(token);
        } catch (ExpiredJwtException e) {
            isExpired = true;
        }

        // Then
        assertThat(isExpired).isTrue();
    }

    @Test
    @DisplayName("JWT 파싱 실패 테스트 - 잘못된 토큰")
    void invalidJwtTest() {
        // Given
        String invalidToken = "invalid.token.here";

        // When & Then
        assertThrows(Exception.class, () -> jwtUtil.getCategory(invalidToken));
        assertThrows(Exception.class, () -> jwtUtil.getUsername(invalidToken));
        assertThrows(Exception.class, () -> jwtUtil.getRole(invalidToken));
        assertThrows(Exception.class, () -> jwtUtil.isExpired(invalidToken));
    }
}