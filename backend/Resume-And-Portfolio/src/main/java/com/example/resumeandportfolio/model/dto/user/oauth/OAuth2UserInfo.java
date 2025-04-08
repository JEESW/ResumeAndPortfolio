package com.example.resumeandportfolio.model.dto.user.oauth;

import com.example.resumeandportfolio.exception.CustomException;
import com.example.resumeandportfolio.exception.ErrorCode;
import com.example.resumeandportfolio.model.entity.user.User;
import com.example.resumeandportfolio.model.enums.Role;
import java.util.Map;
import lombok.Builder;

/**
 * OAuth2UserInfo
 *
 * @author Ji-Seungwoo
 * @version 1.0
 */

@Builder
public record OAuth2UserInfo(
    String name,
    String email
) {

    public static OAuth2UserInfo of(String registrationId, Map<String, Object> attributes) {
        return switch (registrationId) {
            case "google" -> ofGoogle(attributes);
            default -> throw new CustomException(ErrorCode.ILLEGAL_REGISTRATION_ID);
        };
    }

    private static OAuth2UserInfo ofGoogle(Map<String, Object> attributes) {
        return OAuth2UserInfo.builder()
            .name((String) attributes.get("name"))
            .email((String) attributes.get("email"))
            .build();
    }

    public User toEntity() {
        return User.builder()
            .email(email)
            .nickname(name)
            .role(Role.VISITOR)
            .build();
    }
}