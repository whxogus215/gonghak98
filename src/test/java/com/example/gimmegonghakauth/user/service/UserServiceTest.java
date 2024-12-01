package com.example.gimmegonghakauth.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.gimmegonghakauth.user.domain.UserDomain;
import com.example.gimmegonghakauth.user.service.exception.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class UserServiceTest {

    private final Long id = 10000101L;
    private final String password = "test";
    private final String email = "test@gmail.com";
    private final String name = "test_user";

    @Autowired
    private UserService userService;

    private UserDomain user;

    @BeforeEach
    void createUser(TestInfo testInfo) {
        if (testInfo.getTags().contains("setupRequired")) {
            user = userService.create(String.valueOf(id), password, email, null, name);
        }
    }

    @Test
    void create로_유저를_생성할_수_있다() {
        //givne, when
        UserDomain user = userService.create(String.valueOf(id), password, email, null, name);

        //then
        assertThat(user.getId()).isNotNull();
    }

    @Test
    @Tag("setupRequired")
    void updatePassword로_유저의_비밀번호를_변경할_수_있다() {
        //given
        String newPassword = "test123";

        //when
        UserDomain updatedUser = userService.updatePassword(user, newPassword);

        //then
        assertThat(updatedUser.getPassword()).isEqualTo("Fake" + newPassword);
    }

    @Test
    @Tag("setupRequired")
    void 사용자의_학번으로_User를_가져올_수_있다() {
        //when
        UserDomain findUser = userService.getByStudentId(id);

        //then
        assertThat(findUser.getStudentId()).isEqualTo(user.getStudentId());
        assertThat(findUser.getEmail()).isEqualTo(user.getEmail());
        assertThat(findUser.getName()).isEqualTo(user.getName());
    }

    @Test
    @Tag("setupRequired")
    void 사용자의_학번으로_User를_찾지_못하면_예외가_발생한다() {
        //given
        Long wrongId = 10000000L;

        //when & then
        assertThatThrownBy(() -> userService.getByStudentId(wrongId))
            .isInstanceOf(UserNotFoundException.class);
    }
}
