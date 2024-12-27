package com.example.gimmegonghakauth.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.gimmegonghakauth.user.domain.UserDomain;
import com.example.gimmegonghakauth.user.service.dto.ChangePasswordDto;
import com.example.gimmegonghakauth.user.service.dto.UserJoinDto;
import com.example.gimmegonghakauth.user.service.exception.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BeanPropertyBindingResult;

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
        //given, when
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

    @Test
    @Tag("setupRequired")
    void withdrawal로_유저를_삭제할_수_있다() {
        // when
        boolean result = userService.withdrawal(String.valueOf(id), password);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @Tag("setupRequired")
    void withdrawal_비밀번호가_틀리면_삭제되지_않는다() {
        // when
        boolean result = userService.withdrawal(String.valueOf(id), "wrong_password");

        // then
        assertThat(result).isFalse();
    }

    @Test
    void joinValidation_가입검증_성공() {
        // given
        UserJoinDto joinDto = new UserJoinDto(String.valueOf(id), password, password, email, null,
            null, name);
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(joinDto,
            "userJoinDto");

        // when
        boolean isValid = userService.joinValidation(joinDto, bindingResult);

        // then
        assertThat(isValid).isTrue();
        assertThat(bindingResult.hasErrors()).isFalse();
    }

    @Test
    void joinValidation_비밀번호가_일치하지_않으면_가입검증_실패() {
        // given
        String password2 = "wrong_password";
        UserJoinDto joinDto = new UserJoinDto(String.valueOf(id), password, password2, email, null,
            null, name);
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(joinDto,
            "userJoinDto");

        // when
        boolean isValid = userService.joinValidation(joinDto, bindingResult);

        // then
        assertThat(isValid).isFalse();
        assertThat(bindingResult.hasErrors()).isTrue();
    }

    @Test
    @Tag("setupRequired")
    void joinValidation_학번이_중복되면_가입검증_실패() {
        // given
        String email = "test@naver.com";
        UserJoinDto joinDto = new UserJoinDto(String.valueOf(id), password, password, email, null,
            null, name);
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(joinDto,
            "userJoinDto");

        // when
        boolean isValid = userService.joinValidation(joinDto, bindingResult);

        // then
        assertThat(isValid).isFalse();
        assertThat(bindingResult.hasErrors()).isTrue();
    }

    @Test
    @Tag("setupRequired")
    void joinValidation_이메일이_중복되면_가입검증_실패() {
        // given
        Long id = 10000000L;
        UserJoinDto joinDto = new UserJoinDto(String.valueOf(id), password, password, email, null,
            null, name);
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(joinDto,
            "userJoinDto");

        // when
        boolean isValid = userService.joinValidation(joinDto, bindingResult);

        // then
        assertThat(isValid).isFalse();
        assertThat(bindingResult.hasErrors()).isTrue();
    }

    @Test
    @Tag("setupRequired")
    void changePasswordValidation_성공() {
        // given
        ChangePasswordDto changePasswordDto = new ChangePasswordDto(password, "new_password",
            "new_password");
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(changePasswordDto,
            "changePasswordDto");

        // when
        boolean isValid = userService.changePasswordValidation(changePasswordDto, bindingResult,
            user);

        // then
        assertThat(isValid).isTrue();
    }

    @Test
    @Tag("setupRequired")
    void changePasswordValidation_현재_패스워드가_틀리면_실패() {
        // given
        ChangePasswordDto changePasswordDto = new ChangePasswordDto("wrong_password",
            "new_password", "new_password");
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(changePasswordDto,
            "changePasswordDto");

        // when
        boolean isValid = userService.changePasswordValidation(changePasswordDto, bindingResult,
            user);

        // then
        assertThat(isValid).isFalse();
        assertThat(bindingResult.hasErrors()).isTrue();
    }

    @Test
    @Tag("setupRequired")
    void changePasswordValidation_새_패스워드가_일치하지_않으면_실패() {
        // given
        ChangePasswordDto changePasswordDto = new ChangePasswordDto(password, "new_password",
            "mismatch_password");
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(changePasswordDto,
            "changePasswordDto");

        // when
        boolean isValid = userService.changePasswordValidation(changePasswordDto, bindingResult,
            user);

        // then
        assertThat(isValid).isFalse();
        assertThat(bindingResult.hasErrors()).isTrue();
    }
}
