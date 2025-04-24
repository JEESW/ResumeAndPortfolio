package com.example.resumeandportfolio.service.user;

import com.example.resumeandportfolio.exception.CustomException;
import com.example.resumeandportfolio.exception.ErrorCode;
import com.example.resumeandportfolio.model.dto.user.PasswordResetConfirmDto;
import com.example.resumeandportfolio.model.dto.user.PasswordResetRequestDto;
import com.example.resumeandportfolio.model.dto.user.VerificationTokenResponse;
import com.example.resumeandportfolio.model.dto.user.UserLoadInfoDto;
import com.example.resumeandportfolio.model.dto.user.UserLoginResponse;
import com.example.resumeandportfolio.model.dto.user.UserRegisterRequest;
import com.example.resumeandportfolio.model.dto.user.UserRegisterResponse;
import com.example.resumeandportfolio.model.dto.user.UserUpdateRequest;
import com.example.resumeandportfolio.model.dto.user.UserUpdateResponse;
import com.example.resumeandportfolio.model.dto.user.VerificationTokenDto;
import com.example.resumeandportfolio.model.entity.user.User;
import com.example.resumeandportfolio.model.enums.Role;
import com.example.resumeandportfolio.repository.user.UserRepository;
import com.example.resumeandportfolio.util.mail.MailUtil;
import com.example.resumeandportfolio.util.mapper.UserMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * User's Service
 *
 * @author Ji-Seungwoo
 * @version 1.0
 */

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final StringRedisTemplate redisTemplate;
    private final PasswordEncoder passwordEncoder;
    private final MailUtil mailUtil;
    private final ObjectMapper objectMapper;

    @Value("${verification.token.expiration.hours}")
    private int expirationHours;

    // 이메일로 사용자 정보 가져오기
    public UserLoadInfoDto getUserByEmail(String email) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return UserMapper.toUserLoadInfoDto(user);
    }

    // 로그인 로직
    @Transactional
    public UserLoginResponse login(String email, String rawPassword) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 비밀번호 검증
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        return UserMapper.toLoginResponse(user);
    }

    // 회원 가입 이메일 인증 요청 로직
    @Transactional
    public void initiateRegistration(UserRegisterRequest request) {
        // 이메일 중복 체크
        if (userRepository.existsByEmail(request.email())) {
            throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        // 패스워드 재확인 로직
        if (!request.password().equals(request.confirmPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD_CONFIRMATION);
        }

        // VerificationToken 생성 및 Redis 저장
        String token = UUID.randomUUID().toString();
        VerificationTokenDto verificationToken = new VerificationTokenDto(
            token, request.email(), LocalDateTime.now().plusHours(expirationHours)
        );

        try {
            ValueOperations<String, String> valueOps = redisTemplate.opsForValue();
            valueOps.set("verification:token:" + token,
                objectMapper.writeValueAsString(verificationToken),
                Duration.ofHours(expirationHours));
        } catch (Exception e) {
            throw new CustomException(ErrorCode.REDIS_SAVE_ERROR);
        }

        mailUtil.sendVerificationMail(request.email(), token);
    }

    // 인증 이메일 재전송 로직
    @Transactional
    public void resendVerificationEmail(String email) {
        if (!userRepository.existsByEmail(email)) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        // 새로운 VerificationToken 생성 및 Redis 저장
        String token = UUID.randomUUID().toString();
        VerificationTokenDto verificationToken = new VerificationTokenDto(
            token, email, LocalDateTime.now().plusHours(expirationHours)
        );

        try {
            ValueOperations<String, String> valueOps = redisTemplate.opsForValue();
            valueOps.set("verification:token:" + token,
                objectMapper.writeValueAsString(verificationToken),
                Duration.ofHours(expirationHours));
        } catch (Exception e) {
            throw new CustomException(ErrorCode.REDIS_SAVE_ERROR);
        }

        mailUtil.sendVerificationMail(email, token);
    }

    // 이메일 인증 토큰 유효성 검증 로직
    public VerificationTokenResponse verifyToken(String token) {
        ValueOperations<String, String> valueOps = redisTemplate.opsForValue();
        String tokenData = valueOps.get("verification:token:" + token);

        if (tokenData == null) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        try {
            VerificationTokenDto verificationToken = objectMapper.readValue(tokenData,
                VerificationTokenDto.class);
            if (verificationToken.isExpired()) {
                throw new CustomException(ErrorCode.TOKEN_EXPIRED);
            }
            return new VerificationTokenResponse(verificationToken.email(), true);
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.REDIS_PARSE_ERROR);
        }
    }

    // 회원 가입 완료 로직
    @Transactional
    public UserRegisterResponse completeRegistration(String token, String password,
        String nickname) {
        ValueOperations<String, String> valueOps = redisTemplate.opsForValue();
        String tokenData = valueOps.get("verification:token:" + token);

        if (tokenData == null) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        try {
            VerificationTokenDto verificationToken = objectMapper.readValue(tokenData,
                VerificationTokenDto.class);
            if (verificationToken.isExpired()) {
                throw new CustomException(ErrorCode.TOKEN_EXPIRED);
            }

            String encodedPassword = passwordEncoder.encode(password);
            User user = User.builder()
                .email(verificationToken.email())
                .password(encodedPassword)
                .nickname(nickname)
                .role(Role.VISITOR)
                .build();

            User savedUser = userRepository.save(user);
            redisTemplate.delete("verification:token:" + token);

            return UserMapper.toResponse(savedUser);
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.REDIS_PARSE_ERROR);
        }
    }

    // 회원 수정 로직
    @Transactional
    public UserUpdateResponse updateUser(String email, UserUpdateRequest request) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 닉네임과 새 비밀번호가 모두 null인 경우 예외 처리
        if (request.nickname() == null && request.newPassword() == null) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        // 현재 비밀번호 검증
        if (request.currentPassword() != null && !passwordEncoder.matches(request.currentPassword(),
            user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        // 닉네임 업데이트
        if (request.nickname() != null) {
            user.updateNickname(request.nickname());
        }

        // 새 비밀번호 업데이트
        if (request.newPassword() != null) {
            String encodedPassword = passwordEncoder.encode(request.newPassword());
            user.updatePassword(encodedPassword);
        }

        User updatedUser = userRepository.save(user);

        return UserMapper.toUpdateResponse(updatedUser);
    }

    // 회원 탈퇴 로직
    @Transactional
    public void deleteUser(String email) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (user.getDeletedAt() != null) {
            throw new CustomException(ErrorCode.USER_ALREADY_DELETED);
        }

        user.delete();
    }

    // 비밀번호 재설정 요청 로직
    @Transactional
    public void requestPasswordReset(PasswordResetRequestDto request) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(request.email())
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        String token = UUID.randomUUID().toString();
        VerificationTokenDto verificationToken = new VerificationTokenDto(
            token, user.getEmail(), LocalDateTime.now().plusHours(expirationHours)
        );

        try {
            ValueOperations<String, String> valueOps = redisTemplate.opsForValue();
            valueOps.set("password-reset:token:" + token,
                objectMapper.writeValueAsString(verificationToken),
                Duration.ofHours(expirationHours));
        } catch (Exception e) {
            throw new CustomException(ErrorCode.REDIS_SAVE_ERROR);
        }

        mailUtil.sendPasswordResetMail(user.getEmail(), token);
    }

    // 비밀번호 재설정 확인 로직
    @Transactional
    public void confirmPasswordReset(PasswordResetConfirmDto request) {
        ValueOperations<String, String> valueOps = redisTemplate.opsForValue();
        String redisKey = "password-reset:token:" + request.token();
        String tokenData = valueOps.get(redisKey);

        if (tokenData == null) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        try {
            VerificationTokenDto verificationToken = objectMapper.readValue(tokenData,
                VerificationTokenDto.class);
            if (verificationToken.isExpired()) {
                throw new CustomException(ErrorCode.TOKEN_EXPIRED);
            }

            User user = userRepository.findByEmailAndDeletedAtIsNull(verificationToken.email())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
            String encodedPassword = passwordEncoder.encode(request.newPassword());
            user.updatePassword(encodedPassword);
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.REDIS_PARSE_ERROR);
        } finally {
            redisTemplate.delete(redisKey);
        }
    }
}