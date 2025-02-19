package com.example.resumeandportfolio.filter;

import com.example.resumeandportfolio.service.user.RefreshTokenService;
import com.example.resumeandportfolio.util.jwt.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.filter.GenericFilterBean;

/**
 * Custom Logout Filter
 *
 * @author Ji-Seungwoo
 * @version 1.0
 */

@RequiredArgsConstructor
public class CustomLogoutFilter extends GenericFilterBean {

    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        doFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
    }

    private void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        String requestUri = request.getRequestURI();
        if (!requestUri.matches("^/api/users/logout$")) {
            filterChain.doFilter(request, response);
            return;
        }

        String requestMethod = request.getMethod();
        if (!requestMethod.equalsIgnoreCase("POST")) {
            response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }

        // Authorization 헤더에서 Access Token 추출
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Access token not found");
            return;
        }
        String accessToken = authorizationHeader.replace("Bearer ", "");

        // JWT에서 사용자 이메일 추출
        String email;
        try {
            email = jwtUtil.getUsername(accessToken);
        } catch (ExpiredJwtException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Access token expired");
            return;
        }

        // Redis에서 Refresh Token 삭제
        String storedToken = refreshTokenService.getRefreshToken(email);
        if (storedToken == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Refresh token not found");
            return;
        }

        refreshTokenService.deleteRefreshToken(email);
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write("Logout successful");
    }
}