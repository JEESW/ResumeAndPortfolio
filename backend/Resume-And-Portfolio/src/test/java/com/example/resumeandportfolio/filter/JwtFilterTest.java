package com.example.resumeandportfolio.filter;

import com.example.resumeandportfolio.model.enums.Role;
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
 * JWT Authentication Filter Test
 *
 * @author Ji-Seungwoo
 * @version 1.0
 */

class JwtFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private JwtFilter jwtFilter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("JWT 인증 성공 테스트 - 유효한 Access Token")
    void jwtAuthenticationSuccessTest() throws ServletException, IOException {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        String accessToken = "validAccessToken";
        String email = "test@example.com";
        Role role = Role.VISITOR;

        request.addHeader("Authorization", "Bearer " + accessToken);

        when(jwtUtil.isExpired(accessToken)).thenReturn(false);
        when(jwtUtil.getCategory(accessToken)).thenReturn("access");
        when(jwtUtil.getUsername(accessToken)).thenReturn(email);
        when(jwtUtil.getRole(accessToken)).thenReturn(role.name());

        // When
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
        verify(jwtUtil, times(1)).isExpired(accessToken);
        verify(jwtUtil, times(1)).getCategory(accessToken);
        verify(jwtUtil, times(1)).getUsername(accessToken);
        verify(jwtUtil, times(1)).getRole(accessToken);
    }

    @Test
    @DisplayName("JWT 인증 실패 테스트 - Authorization 헤더 없음")
    void jwtAuthenticationFailureNoAuthorizationHeaderTest() throws ServletException, IOException {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        // When
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK); // 다음 필터로 전달
        verifyNoInteractions(jwtUtil);
    }

    @Test
    @DisplayName("JWT 인증 실패 테스트 - 만료된 토큰")
    void jwtAuthenticationFailureExpiredTokenTest() throws ServletException, IOException {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        String expiredToken = "expiredAccessToken";
        request.addHeader("Authorization", "Bearer " + expiredToken);

        doThrow(new ExpiredJwtException(null, null, "Token expired")).when(jwtUtil).isExpired(expiredToken);

        // When
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
        assertThat(response.getContentAsString()).contains("Access token expired");
        verify(jwtUtil, times(1)).isExpired(expiredToken);
        verify(jwtUtil, never()).getCategory(anyString());
    }

    @Test
    @DisplayName("JWT 인증 실패 테스트 - 잘못된 토큰 카테고리")
    void jwtAuthenticationFailureInvalidCategoryTest() throws ServletException, IOException {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        String invalidToken = "invalidAccessToken";
        request.addHeader("Authorization", "Bearer " + invalidToken);

        when(jwtUtil.isExpired(invalidToken)).thenReturn(false);
        when(jwtUtil.getCategory(invalidToken)).thenReturn("refresh");

        // When
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
        assertThat(response.getContentAsString()).contains("Invalid access token");
        verify(jwtUtil, times(1)).isExpired(invalidToken);
        verify(jwtUtil, times(1)).getCategory(invalidToken);
        verify(jwtUtil, never()).getUsername(anyString());
    }
}