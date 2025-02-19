package com.example.resumeandportfolio.controller.user;

import com.example.resumeandportfolio.service.user.RefreshTokenService;
import com.example.resumeandportfolio.util.jwt.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Refresh Token Reissue Controller
 *
 * @author Ji-Seungwoo
 * @version 1.0
 */

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class ReissueController {

    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    // JWT 토큰 재발급
    @PostMapping("/reissue")
    public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) {
        String accessToken = request.getHeader("Authorization").replace("Bearer ", "");
        String email;
        try {
            email = jwtUtil.getUsername(accessToken);
        } catch (ExpiredJwtException e) {
            return new ResponseEntity<>("Access token expired. Please log in again.",
                HttpStatus.UNAUTHORIZED);
        }

        String refresh = refreshTokenService.getRefreshToken(email);

        // Refresh 토큰 확인
        if (refresh == null) {
            return new ResponseEntity<>("Refresh token not found", HttpStatus.BAD_REQUEST);
        }

        // 만료 여부 확인
        try {
            jwtUtil.isExpired(refresh);
        } catch (ExpiredJwtException e) {
            return new ResponseEntity<>("Refresh token expired", HttpStatus.UNAUTHORIZED);
        }

        // 토큰이 refresh인지 확인
        if (!"refresh".equals(jwtUtil.getCategory(refresh))) {
            return new ResponseEntity<>("Invalid refresh token", HttpStatus.BAD_REQUEST);
        }

        // 새로운 Access Token 및 Refresh Token 생성
        String role = jwtUtil.getRole(refresh);
        String newAccessToken = jwtUtil.createJwt("access", email, role, 600000L);
        String newRefreshToken = jwtUtil.createJwt("refresh", email, role, 86400000L);

        // Redis에 기존의 Refresh 토큰 삭제 후 새 Refresh 토큰 저장
        refreshTokenService.deleteRefreshToken(email);
        refreshTokenService.saveRefreshToken(email, newRefreshToken, 86400L);

        // 새로운 Access Token을 헤더에 추가
        response.setHeader("Authorization", "Bearer " + newAccessToken);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    // OAuth 로그인 시 One-Time Code를 확인하고 새로운 Access Token과 Refresh Token을 발급
    @PostMapping("/oauth2/token")
    public ResponseEntity<?> issueOAuth2Token(@RequestParam String code, HttpServletResponse response) {
        // Redis에서 One-Time Code로 이메일 조회
        String email = refreshTokenService.getEmailByOneTimeCode(code);

        if (email == null) {
            return new ResponseEntity<>("Invalid or expired one-time code", HttpStatus.BAD_REQUEST);
        }

        // JWT 생성
        String newAccessToken = jwtUtil.createJwt("access", email, "ROLE_VISITOR", 600000L);
        String newRefreshToken = jwtUtil.createJwt("refresh", email, "ROLE_VISITOR", 86400000L);

        // One-Time Code 삭제
        refreshTokenService.deleteOneTimeCode(code);

        // Redis에 Refresh Token 저장
        refreshTokenService.saveRefreshToken(email, newRefreshToken, 86400L);

        // Access Token 반환
        response.setHeader("Authorization", "Bearer " + newAccessToken);
        return ResponseEntity.ok().body("{\"accessToken\": \"" + newAccessToken + "\"}");
    }
}