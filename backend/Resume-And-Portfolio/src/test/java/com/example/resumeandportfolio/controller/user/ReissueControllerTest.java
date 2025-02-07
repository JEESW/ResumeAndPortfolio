package com.example.resumeandportfolio.controller.user;

import com.example.resumeandportfolio.service.user.RefreshTokenService;
import com.example.resumeandportfolio.util.jwt.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockCookie;
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
    @DisplayName("토큰 재발급 실패 테스트 - 쿠키가 없음")
    void reissue_noCookies_returnsBadRequest() throws Exception {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        // When
        var result = reissueController.reissue(request, response);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(result.getBody()).isEqualTo("No cookies found");
    }

    @Test
    @DisplayName("토큰 재발급 실패 테스트 - Refresh 토큰이 null")
    void reissue_refreshTokenNull_returnsBadRequest() throws Exception {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setCookies(new MockCookie("otherCookie", "someValue"));

        // When
        var result = reissueController.reissue(request, response);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(result.getBody()).isEqualTo("refresh token null");
    }

    @Test
    @DisplayName("토큰 재발급 실패 테스트 - Refresh 토큰이 만료됨")
    void reissue_expiredToken_returnsBadRequest() throws Exception {
        // Given
        String expiredToken = "expiredRefreshToken";
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setCookies(new MockCookie("refresh", expiredToken));

        doThrow(ExpiredJwtException.class).when(jwtUtil).isExpired(expiredToken);

        // When
        var result = reissueController.reissue(request, response);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(result.getBody()).isEqualTo("refresh token expired");
    }

    @Test
    @DisplayName("토큰 재발급 실패 테스트- Refresh 토큰이 유효하지 않음")
    void reissue_invalidToken_returnsBadRequest() throws Exception {
        // Given
        String invalidToken = "invalidRefreshToken";
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setCookies(new MockCookie("refresh", invalidToken));

        when(jwtUtil.getCategory(invalidToken)).thenReturn("invalid");

        // When
        var result = reissueController.reissue(request, response);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(result.getBody()).isEqualTo("invalid refresh token");
    }

    @Test
    @DisplayName("토큰 재발급 실패 테스트 - 저장된 토큰과 일치하지 않음")
    void reissue_mismatchedToken_returnsBadRequest() throws Exception {
        // Given
        String validToken = "validRefreshToken";
        String mismatchedToken = "mismatchedToken";
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setCookies(new MockCookie("refresh", validToken));

        when(jwtUtil.getCategory(validToken)).thenReturn("refresh");
        when(jwtUtil.getUsername(validToken)).thenReturn("user@example.com");
        when(refreshTokenService.getRefreshToken("user@example.com")).thenReturn(mismatchedToken);

        // When
        var result = reissueController.reissue(request, response);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(result.getBody()).isEqualTo("invalid refresh token");
    }

    @Test
    @DisplayName("토큰 재발급 성공 테스트")
    void reissue_success_generatesNewTokens() throws Exception {
        // Given
        String validToken = "validRefreshToken";
        String username = "user@example.com";
        String role = "USER";
        String newAccessToken = "newAccessToken";
        String newRefreshToken = "newRefreshToken";
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        MockCookie refreshCookie = new MockCookie("refresh", validToken);
        request.setCookies(refreshCookie);

        when(jwtUtil.isExpired(validToken)).thenReturn(false);
        when(jwtUtil.getCategory(validToken)).thenReturn("refresh");
        when(jwtUtil.getUsername(validToken)).thenReturn(username);
        when(jwtUtil.getRole(validToken)).thenReturn(role);
        when(refreshTokenService.getRefreshToken(username)).thenReturn(validToken);
        when(jwtUtil.createJwt("access", username, role, 600000L)).thenReturn(newAccessToken);
        when(jwtUtil.createJwt("refresh", username, role, 86400000L)).thenReturn(newRefreshToken);

        // When
        var result = reissueController.reissue(request, response);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeader("Authorization")).isEqualTo("Bearer " + newAccessToken);

        Cookie responseCookie = response.getCookie("refresh");
        assertThat(responseCookie).isNotNull();
        assertThat(responseCookie.getValue()).isEqualTo(newRefreshToken);
        assertThat(responseCookie.isHttpOnly()).isTrue();
        assertThat(responseCookie.getSecure()).isTrue();
        assertThat(responseCookie.getMaxAge()).isEqualTo(24*60*60);

        verify(refreshTokenService).deleteRefreshToken(username);
        verify(refreshTokenService).saveRefreshToken(username, newRefreshToken, 86400000L);
    }
}