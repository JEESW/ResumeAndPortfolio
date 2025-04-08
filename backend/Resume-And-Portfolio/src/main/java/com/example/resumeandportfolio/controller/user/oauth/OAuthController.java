package com.example.resumeandportfolio.controller.user.oauth;

import com.example.resumeandportfolio.exception.CustomException;
import com.example.resumeandportfolio.exception.ErrorCode;
import com.example.resumeandportfolio.model.entity.user.User;
import com.example.resumeandportfolio.repository.user.UserRepository;
import com.example.resumeandportfolio.service.user.RefreshTokenService;
import com.example.resumeandportfolio.util.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * OAuth2 Controller
 *
 * @author Ji-Seungwoo
 * @version 1.0
 */

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/oauth2")
public class OAuthController {

    private final RefreshTokenService refreshTokenService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @PostMapping("/token")
    public ResponseEntity<?> issueToken(@RequestParam String code) {
        // 1. Redis에 저장된 일회용 코드 조회
        String email = refreshTokenService.getUsernameByOneTimeCode(code);
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired code");
        }

        // 2. 사용자 정보 확인
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        String role = user.getRole().name();
        String nickname = user.getNickname();

        // 3. JWT 생성
        String accessToken = jwtUtil.createJwt("access", email, role, nickname, 600000L);
        String refreshToken = jwtUtil.createJwt("refresh", email, role, nickname, 86400000L);

        // 4. RefreshToken 저장 및 일회용 코드 삭제
        refreshTokenService.saveRefreshToken(email, refreshToken, 86400L);
        refreshTokenService.deleteOneTimeCode(code);

        // 5. 응답
        return ResponseEntity.ok()
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .build();
    }
}