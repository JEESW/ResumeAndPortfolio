package com.example.resumeandportfolio.filter;

import com.example.resumeandportfolio.service.user.RefreshTokenService;
import com.example.resumeandportfolio.util.jwt.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Custom Logout Filter Test
 *
 * @author Ji-Seungwoo
 * @version 1.0
 */

class CustomLogoutFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private CustomLogoutFilter customLogoutFilter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("로그아웃 성공 테스트 - Refresh Token이 유효하고 삭제된 경우")
    void logoutSuccessTest() throws IOException, ServletException {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/users/logout");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        String accessToken = "validAccessToken";
        String email = "user@example.com";

        request.addHeader("Authorization", "Bearer " + accessToken);

        when(jwtUtil.getUsername(accessToken)).thenReturn(email);
        when(refreshTokenService.getRefreshToken(email)).thenReturn("validRefreshToken");

        // When
        customLogoutFilter.doFilter(request, response, filterChain);

        // Then
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
        assertThat(response.getContentAsString()).isEqualTo("Logout successful");
        verify(refreshTokenService, times(1)).deleteRefreshToken(email);
    }

    @Test
    @DisplayName("로그아웃 실패 테스트 - Authorization 헤더 없음")
    void logoutFailureNoAuthorizationHeaderTest() throws IOException, ServletException {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/users/logout");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        // When
        customLogoutFilter.doFilter(request, response, filterChain);

        // Then
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_BAD_REQUEST);
        assertThat(response.getContentAsString()).isEqualTo("Access token not found");
        verifyNoInteractions(jwtUtil, refreshTokenService);
    }

    @Test
    @DisplayName("로그아웃 실패 테스트 - Access Token 만료")
    void logoutFailureExpiredAccessTokenTest() throws IOException, ServletException {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/users/logout");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        String accessToken = "expiredAccessToken";
        request.addHeader("Authorization", "Bearer " + accessToken);

        doThrow(ExpiredJwtException.class).when(jwtUtil).getUsername(accessToken);

        // When
        customLogoutFilter.doFilter(request, response, filterChain);

        // Then
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
        assertThat(response.getContentAsString()).isEqualTo("Access token expired");
        verify(jwtUtil, times(1)).getUsername(accessToken);
        verifyNoInteractions(refreshTokenService);
    }

    @Test
    @DisplayName("로그아웃 실패 테스트 - Redis에 Refresh Token 없음")
    void logoutFailureNoRefreshTokenInRedisTest() throws IOException, ServletException {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/users/logout");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        String accessToken = "validAccessToken";
        String email = "user@example.com";

        request.addHeader("Authorization", "Bearer " + accessToken);

        when(jwtUtil.getUsername(accessToken)).thenReturn(email);
        when(refreshTokenService.getRefreshToken(email)).thenReturn(null);

        // When
        customLogoutFilter.doFilter(request, response, filterChain);

        // Then
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_BAD_REQUEST);
        assertThat(response.getContentAsString()).isEqualTo("Refresh token not found");
        verify(refreshTokenService, times(1)).getRefreshToken(email);
    }

    @Test
    @DisplayName("로그아웃 실패 테스트 - 잘못된 요청 메서드")
    void logoutFailureInvalidMethodTest() throws IOException, ServletException {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/users/logout");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        // When
        customLogoutFilter.doFilter(request, response, filterChain);

        // Then
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        verifyNoInteractions(jwtUtil, refreshTokenService);
    }
}