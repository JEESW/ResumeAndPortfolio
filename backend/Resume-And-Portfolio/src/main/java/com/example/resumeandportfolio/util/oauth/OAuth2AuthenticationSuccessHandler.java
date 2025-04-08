package com.example.resumeandportfolio.util.oauth;

import com.example.resumeandportfolio.service.user.RefreshTokenService;
import com.example.resumeandportfolio.util.jwt.JwtUtil;
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

    private static final String REDIRECT_URL = "https://www.jsw-resumeandportfolio.com/oauth2/callback";

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String username = oAuth2User.getAttribute("email");

        // One-Time Code 생성 및 Redis 저장 (5분 유효)
        String oneTimeCode = UUID.randomUUID().toString();
        refreshTokenService.saveOneTimeCode(oneTimeCode, username, 300);

        // 클라이언트를 프론트엔드 콜백 URL로 리디렉트, code 포함
        String targetUrl = REDIRECT_URL + "?code=" + oneTimeCode;
        response.sendRedirect(targetUrl);
    }
}