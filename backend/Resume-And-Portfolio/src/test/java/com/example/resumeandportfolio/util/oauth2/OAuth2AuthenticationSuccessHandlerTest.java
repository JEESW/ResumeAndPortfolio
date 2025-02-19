package com.example.resumeandportfolio.util.oauth2;

import com.example.resumeandportfolio.service.user.RefreshTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Collections;

import static org.mockito.Mockito.*;

/**
 * OAuth2 Authentication Success Handler Test
 *
 * @author Ji-Seungwoo
 * @version 1.0
 */

class OAuth2AuthenticationSuccessHandlerTest {

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private OAuth2AuthenticationSuccessHandler successHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("OAuth2 인증 성공 시 One-Time Code 생성 및 리다이렉트")
    void onAuthenticationSuccess_generatesOneTimeCodeAndRedirects() throws IOException {
        // Given
        String email = "test@example.com";
        OAuth2User mockOAuth2User = new DefaultOAuth2User(
            Collections.emptySet(),
            Collections.singletonMap("email", email),
            "email"
        );

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            mockOAuth2User,
            null,
            mockOAuth2User.getAuthorities()
        );

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        // When
        successHandler.onAuthenticationSuccess(request, response, authentication);

        // Then
        verify(refreshTokenService, times(1)).saveOneTimeCode(anyString(), eq(email), eq(300L));
        verify(response, times(1)).sendRedirect(argThat(url ->
            url.startsWith("https://www.jsw-resumeandportfolio.com/oauth/login?code=")
        ));
    }
}