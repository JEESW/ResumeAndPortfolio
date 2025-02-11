package com.example.resumeandportfolio.service.user;

import com.example.resumeandportfolio.model.entity.user.User;
import com.example.resumeandportfolio.model.enums.Role;
import com.example.resumeandportfolio.repository.user.UserRepository;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.stereotype.Service;

/**
 * Custom OAuth2 User Service
 *
 * @author Ji-Seungwoo
 * @version 1.0
 */

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");

        User user = registerUserIfNotExists(email, name);

        return new DefaultOAuth2User(
            Collections.singleton(new OAuth2UserAuthority("ROLE_" + user.getRole().name(),
                oauth2User.getAttributes())),
            oauth2User.getAttributes(),
            "email"
        );
    }

    // 첫 로그인 시 DB 등록
    private User registerUserIfNotExists(String email, String name) {
        return userRepository.findByEmailAndDeletedAtIsNull(email)
            .orElseGet(() -> userRepository.save(User.builder()
                .email(email)
                .nickname(name)
                .password("")
                .role(Role.VISITOR)
                .build()));
    }
}