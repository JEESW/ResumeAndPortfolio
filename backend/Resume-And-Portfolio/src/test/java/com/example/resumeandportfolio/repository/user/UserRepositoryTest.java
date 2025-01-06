package com.example.resumeandportfolio.repository.user;

import com.example.resumeandportfolio.model.entity.user.User;
import com.example.resumeandportfolio.model.enums.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("이메일로 사용자 찾기 - 성공")
    void findByEmailSuccess() {
        // Given: 테스트 데이터 삽입
        User user = User.builder()
            .email("test@example.com")
            .password("encoded_password")
            .nickname("Tester")
            .role(Role.VISITOR)
            .build();
        userRepository.save(user);

        // When: 이메일로 사용자 조회
        Optional<User> result = userRepository.findByEmail("test@example.com");

        // Then: 결과 검증
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("test@example.com");
        assertThat(result.get().getNickname()).isEqualTo("Tester");
        assertThat(result.get().getRole()).isEqualTo(Role.VISITOR);
    }

    @Test
    @DisplayName("이메일로 사용자 찾기 - 실패")
    void findByEmailFailure() {
        // When: 존재하지 않는 이메일로 사용자 조회
        Optional<User> result = userRepository.findByEmail("notfound@example.com");

        // Then: 결과 검증
        assertThat(result).isNotPresent();
    }
}