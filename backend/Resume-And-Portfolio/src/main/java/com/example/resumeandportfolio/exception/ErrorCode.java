package com.example.resumeandportfolio.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * Error Code
 *
 * @author Ji-Seungwoo
 * @version 1.0
 */

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "INVALID_PASSWORD", "비밀번호가 일치하지 않습니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "EMAIL_ALREADY_EXISTS", "이미 사용 중인 이메일입니다."),
    INVALID_PASSWORD_CONFIRMATION(HttpStatus.BAD_REQUEST, "INVALID_PASSWORD_CONFIRMATION", "비밀번호가 일치하지 않습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "로그인이 필요합니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "요청이 유효하지 않습니다."),
    USER_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "USER_ALREADY_DELETED", "이미 탈퇴한 사용자입니다."),
    REDIS_SAVE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "REDIS_SAVE_ERROR", "Redis 저장 중 오류가 발생했습니다."),
    INVALID_TOKEN(HttpStatus.BAD_REQUEST, "INVALID_TOKEN", "유효하지 않은 토큰입니다."),
    TOKEN_EXPIRED(HttpStatus.GONE, "TOKEN_EXPIRED", "토큰이 만료되었습니다."),
    ILLEGAL_REGISTRATION_ID(HttpStatus.NOT_FOUND, "ILLEGAL_REGISTRATION_ID", "해당 계정을 찾을 수 없습니다."),
    REDIS_PARSE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "REDIS_PARSE_ERROR", "Redis 데이터 처리 중 오류가 발생했습니다.");

    private final HttpStatus status;  // HTTP 상태 코드
    private final String code;    // 에러 코드
    private final String message; // 에러 메시지
}