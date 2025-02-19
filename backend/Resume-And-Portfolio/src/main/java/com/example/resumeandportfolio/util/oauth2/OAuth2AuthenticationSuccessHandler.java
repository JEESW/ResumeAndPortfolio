package com.example.resumeandportfolio.util.oauth2;

import com.example.resumeandportfolio.service.user.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

/**
 * OAuth2 Authentication Success Handler
 *
 * @author Ji-Seungwoo
 * @version 1.0
 */

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final RefreshTokenService refreshTokenService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");

        // One-Time Code 생성
        String oneTimeCode = UUID.randomUUID().toString();

        // One-Time Code Redis에 저장
        refreshTokenService.saveOneTimeCode(oneTimeCode, email, 300);

        // 클라이언트를 /oauth2/callback/google?code=oneTimeCode로 리다이렉트
        String targetUrl = "https://www.jsw-resumeandportfolio.com/api/users/oauth2/callback/google?code=" + oneTimeCode;
        response.sendRedirect(targetUrl);
    }
}