package com.example.resumeandportfolio.controller.user;

import com.example.resumeandportfolio.service.user.RefreshTokenService;
import com.example.resumeandportfolio.util.jwt.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Refresh Token Reissue Controller Test
 *
 * @author Ji-Seungwoo
 * @version 1.0
 */

class ReissueControllerTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private ReissueController reissueController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("재발급 실패 테스트 - Access Token이 만료됨")
    void reissue_accessTokenExpired_returnsUnauthorized() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("Authorization", "Bearer expiredAccessToken");

        doThrow(ExpiredJwtException.class).when(jwtUtil).getUsername("expiredAccessToken");

        // When
        var result = reissueController.reissue(request, response);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(result.getBody()).isEqualTo("Access token expired. Please log in again.");
    }

    @Test
    @DisplayName("재발급 실패 테스트 - Refresh Token이 없음")
    void reissue_refreshTokenNotFound_returnsBadRequest() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("Authorization", "Bearer validAccessToken");

        when(jwtUtil.getUsername("validAccessToken")).thenReturn("user@example.com");
        when(refreshTokenService.getRefreshToken("user@example.com")).thenReturn(null);

        // When
        var result = reissueController.reissue(request, response);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(result.getBody()).isEqualTo("Refresh token not found");
    }

    @Test
    @DisplayName("재발급 실패 테스트 - Refresh Token이 만료됨")
    void reissue_expiredRefreshToken_returnsUnauthorized() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("Authorization", "Bearer validAccessToken");

        when(jwtUtil.getUsername("validAccessToken")).thenReturn("user@example.com");
        when(refreshTokenService.getRefreshToken("user@example.com")).thenReturn("expiredRefreshToken");
        doThrow(ExpiredJwtException.class).when(jwtUtil).isExpired("expiredRefreshToken");

        // When
        var result = reissueController.reissue(request, response);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(result.getBody()).isEqualTo("Refresh token expired");
    }

    @Test
    @DisplayName("재발급 성공 테스트")
    void reissue_success_returnsNewAccessToken() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("Authorization", "Bearer validAccessToken");

        String email = "user@example.com";
        String role = "USER";
        String newAccessToken = "newAccessToken";
        String newRefreshToken = "newRefreshToken";

        when(jwtUtil.getUsername("validAccessToken")).thenReturn(email);
        when(refreshTokenService.getRefreshToken(email)).thenReturn("validRefreshToken");
        when(jwtUtil.isExpired("validRefreshToken")).thenReturn(false);
        when(jwtUtil.getCategory("validRefreshToken")).thenReturn("refresh");
        when(jwtUtil.getRole("validRefreshToken")).thenReturn(role);
        when(jwtUtil.createJwt("access", email, role, 600000L)).thenReturn(newAccessToken);
        when(jwtUtil.createJwt("refresh", email, role, 86400000L)).thenReturn(newRefreshToken);

        // When
        var result = reissueController.reissue(request, response);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeader("Authorization")).isEqualTo("Bearer " + newAccessToken);
        verify(refreshTokenService).deleteRefreshToken(email);
        verify(refreshTokenService).saveRefreshToken(email, newRefreshToken, 86400L);
    }
}