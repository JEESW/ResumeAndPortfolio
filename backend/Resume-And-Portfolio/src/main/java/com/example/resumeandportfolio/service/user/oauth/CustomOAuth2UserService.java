package com.example.resumeandportfolio.service.user.oauth;

import com.example.resumeandportfolio.model.dto.user.oauth.OAuth2PrincipalDetails;
import com.example.resumeandportfolio.model.dto.user.oauth.OAuth2UserInfo;
import com.example.resumeandportfolio.model.entity.user.User;
import com.example.resumeandportfolio.repository.user.UserRepository;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        Map<String, Object> oAuth2UserAttributes = super.loadUser(userRequest).getAttributes();

        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails()
            .getUserInfoEndpoint().getUserNameAttributeName();

        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfo.of(registrationId, oAuth2UserAttributes);

        User user = getOrSave(oAuth2UserInfo);

        return new OAuth2PrincipalDetails(user, oAuth2UserAttributes, userNameAttributeName);
    }

    // 첫 로그인 시 DB 등록
    private User getOrSave(OAuth2UserInfo oAuth2UserInfo) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(oAuth2UserInfo.email())
            .orElseGet(oAuth2UserInfo::toEntity);
        return userRepository.save(user);
    }
}