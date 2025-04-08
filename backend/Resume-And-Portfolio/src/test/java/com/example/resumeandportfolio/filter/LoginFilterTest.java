package com.example.resumeandportfolio.filter;

import com.example.resumeandportfolio.model.entity.user.CustomUserDetails;
import com.example.resumeandportfolio.model.entity.user.User;
import com.example.resumeandportfolio.model.enums.Role;
import com.example.resumeandportfolio.service.user.RefreshTokenService;
import com.example.resumeandportfolio.util.jwt.JwtUtil;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.io.IOException;
import org.springframework.security.core.AuthenticationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Login Filter Test
 *
 * @author Ji-Seungwoo
 * @version 1.0
 */

class LoginFilterTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private LoginFilter loginFilter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("로그인 성공 테스트")
    void successfulLoginTest() throws ServletException, IOException {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        request.setParameter("username", "test@example.com");
        request.setParameter("password", "password123");

        CustomUserDetails userDetails = new CustomUserDetails(
            User.builder()
                .email("test@example.com")
                .password("encoded_password")
                .nickname("test")
                .role(Role.VISITOR)
                .build()
        );

        Authentication authToken = new UsernamePasswordAuthenticationToken(userDetails, null,
            userDetails.getAuthorities());
        when(authenticationManager.authenticate(any())).thenReturn(authToken);

        String accessToken = "accessToken";
        String refreshToken = "refreshToken";

        when(jwtUtil.createJwt("access", "test@example.com", "ROLE_VISITOR", "test",
            600000L)).thenReturn(accessToken);
        when(jwtUtil.createJwt("refresh", "test@example.com", "ROLE_VISITOR", "test",
            86400000L)).thenReturn(refreshToken);

        // When
        loginFilter.attemptAuthentication(request, response);
        loginFilter.successfulAuthentication(request, response, filterChain, authToken);

        // Then
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
        assertThat(response.getHeader("Authorization")).isEqualTo("Bearer " + accessToken);
        verify(refreshTokenService, times(1)).saveRefreshToken("test@example.com", refreshToken,
            86400L);
    }

    @Test
    @DisplayName("로그인 실패 테스트 - 잘못된 인증 정보")
    void unsuccessfulLoginTest_InvalidCredentials() throws IOException, ServletException {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.setParameter("username", "test@example.com");
        request.setParameter("password", "wrongPassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new BadCredentialsException("Invalid credentials"));

        // When
        try {
            loginFilter.attemptAuthentication(request, response);
        } catch (AuthenticationException e) {
            loginFilter.unsuccessfulAuthentication(request, response, e);
        }

        // Then
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
        assertThat(response.getContentAsString()).contains("Invalid username or password");
        verifyNoInteractions(jwtUtil, refreshTokenService);
    }

    @Test
    @DisplayName("로그인 실패 테스트 - 사용자 인증 실패")
    void unsuccessfulLoginTest_AuthenticationFailure() throws IOException, ServletException {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.setParameter("username", "unknown@example.com");
        request.setParameter("password", "password123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new BadCredentialsException("Authentication failed"));

        // When
        try {
            loginFilter.attemptAuthentication(request, response);
        } catch (AuthenticationException e) {
            loginFilter.unsuccessfulAuthentication(request, response, e);
        }

        // Then
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
        assertThat(response.getContentAsString()).contains("Invalid username or password");
        verifyNoInteractions(jwtUtil, refreshTokenService);
    }
}