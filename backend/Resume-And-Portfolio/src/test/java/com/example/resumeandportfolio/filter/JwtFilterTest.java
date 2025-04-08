package com.example.resumeandportfolio.filter;

import com.example.resumeandportfolio.model.enums.Role;
import com.example.resumeandportfolio.service.user.RefreshTokenService;
import com.example.resumeandportfolio.util.jwt.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
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
 * JWT Authentication Filter Test
 *
 * @author Ji-Seungwoo
 * @version 1.0
 */

class JwtFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private JwtFilter jwtFilter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        jwtFilter = new JwtFilter(jwtUtil, refreshTokenService);
    }

    @Test
    @DisplayName("JWT 인증 성공 테스트 - 유효한 Access Token")
    void jwtAuthenticationSuccessTest() throws ServletException, IOException {
        // Given
        String accessToken = "validToken";
        String email = "test@example.com";
        String role = Role.VISITOR.name();
        String nickname = "테스트닉네임";

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + accessToken);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        when(jwtUtil.getUsername(accessToken)).thenReturn(email);
        when(jwtUtil.isExpired(accessToken)).thenReturn(false);
        when(jwtUtil.getRole(accessToken)).thenReturn(role);
        when(jwtUtil.getNickname(accessToken)).thenReturn(nickname);

        // When
        jwtFilter.doFilterInternal(request, response, chain);

        // Then
        assertThat(response.getStatus()).isEqualTo(200);
        verify(jwtUtil, times(1)).getUsername(accessToken);
        verify(jwtUtil).isExpired(accessToken);
        verify(jwtUtil).getRole(accessToken);
        verify(jwtUtil).getNickname(accessToken);
    }

    @Test
    @DisplayName("JWT 인증 실패 테스트 - Authorization 헤더 없음")
    void jwtAuthenticationFailureNoHeaderTest() throws ServletException, IOException {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        // When
        jwtFilter.doFilterInternal(request, response, chain);

        // Then
        assertThat(response.getStatus()).isEqualTo(200);
        verifyNoInteractions(jwtUtil);
    }

    @Test
    @DisplayName("JWT 인증 실패 테스트 - 만료된 Access Token + 유효한 Refresh Token")
    void jwtAuthenticationExpiredAccessTokenWithValidRefreshToken()
        throws ServletException, IOException {
        // Given
        String expiredToken = "expiredToken";
        String email = "test@example.com";
        String role = Role.VISITOR.name();
        String nickname = "nickname";
        String validRefreshToken = "validRefreshToken";

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + expiredToken);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        ExpiredJwtException expiredJwtException =
            new ExpiredJwtException(null, null, "expired", new Throwable());

        when(jwtUtil.getUsername(expiredToken)).thenThrow(expiredJwtException);
        when(jwtUtil.extractUsernameFromExpiredToken(expiredToken)).thenReturn(email);
        when(refreshTokenService.getRefreshToken(email)).thenReturn(validRefreshToken);
        when(jwtUtil.isExpired(validRefreshToken)).thenReturn(false);
        when(jwtUtil.getCategory(validRefreshToken)).thenReturn("refresh");
        when(jwtUtil.getRole(validRefreshToken)).thenReturn(role);
        when(jwtUtil.getNickname(validRefreshToken)).thenReturn(nickname);
        when(jwtUtil.createJwt("access", email, role, nickname, 600000L)).thenReturn(
            "newAccessToken");
        when(jwtUtil.createJwt("refresh", email, role, nickname, 86400000L)).thenReturn(
            "newRefreshToken");

        // When
        jwtFilter.doFilterInternal(request, response, chain);

        // Then
        assertThat(response.getHeader("Authorization")).isEqualTo("Bearer newAccessToken");
        verify(refreshTokenService).deleteRefreshToken(email);
        verify(refreshTokenService).saveRefreshToken(eq(email), eq("newRefreshToken"), anyLong());
    }

    @Test
    @DisplayName("JWT 인증 실패 테스트 - 만료된 Access Token + 없는 Refresh Token")
    void jwtAuthenticationExpiredAccessTokenNoRefreshToken() throws ServletException, IOException {
        // Given
        String expiredToken = "expiredToken";
        String email = "test@example.com";

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + expiredToken);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        ExpiredJwtException expiredJwtException = new ExpiredJwtException(null, null, "expired",
            new Throwable());

        when(jwtUtil.getUsername(expiredToken)).thenThrow(expiredJwtException);
        when(jwtUtil.extractUsernameFromExpiredToken(expiredToken)).thenReturn(email);
        when(refreshTokenService.getRefreshToken(email)).thenReturn(null);

        // When
        jwtFilter.doFilterInternal(request, response, chain);

        // Then
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
        assertThat(response.getErrorMessage()).isEqualTo("Invalid or expired refresh token");
    }
}