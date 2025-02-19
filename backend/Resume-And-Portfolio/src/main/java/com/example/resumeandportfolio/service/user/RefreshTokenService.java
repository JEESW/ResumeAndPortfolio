package com.example.resumeandportfolio.service.user;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Refresh Token Service
 *
 * @author Ji-Seungwoo
 * @version 1.0
 */

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final StringRedisTemplate redisTemplate;

    // Refresh Token 저장
    public void saveRefreshToken(String username, String refreshToken, long duration) {
        redisTemplate.opsForValue()
            .set("refresh:" + username, refreshToken, duration, TimeUnit.SECONDS);
    }

    // Refresh Token 조회
    public String getRefreshToken(String username) {
        return redisTemplate.opsForValue().get("refresh:" + username);
    }

    // Refresh Token 삭제
    public void deleteRefreshToken(String username) {
        redisTemplate.delete("refresh:" + username);
    }

    // OAuth2 일회성 코드 저장
    public void saveOneTimeCode(String code, String email, long durationInSeconds) {
        redisTemplate.opsForValue()
            .set("one-time-code:" + code, email, durationInSeconds, TimeUnit.SECONDS);
    }

    // OAuth2 일회성 코드 조회
    public String getEmailByOneTimeCode(String code) {
        return redisTemplate.opsForValue().get("one-time-code:" + code);
    }

    // OAuth2 일회성 코드 삭제
    public void deleteOneTimeCode(String code) {
        redisTemplate.delete("one-time-code:" + code);
    }
}