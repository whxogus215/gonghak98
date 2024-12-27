package com.example.gimmegonghakauth.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.gimmegonghakauth.user.domain.UserDomain;
import com.example.gimmegonghakauth.user.service.dto.ChangePasswordDto;
import com.example.gimmegonghakauth.user.service.dto.UserJoinDto;
import com.example.gimmegonghakauth.user.service.exception.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
    @DisplayName("유저 생성 테스트")
    void createUserTest() {
        //given, when
        UserDomain user = userService.create(String.valueOf(id), password, email, null, name);

        //then
        assertThat(user.getId()).isNotNull();
    }

    @Test
    @Tag("setupRequired")
    @DisplayName("비밀번호 변경 테스트")
    void updatePasswordTest() {
        //given
        String newPassword = "test123";

        //when
        UserDomain updatedUser = userService.updatePassword(user, newPassword);

        //then
        assertThat(updatedUser.getPassword()).isEqualTo("Fake" + newPassword);
    }

    @Test
    @Tag("setupRequired")
    @DisplayName("학번으로 유저 찾기 테스트")
    void getByStudentIdTest() {
        //when
        UserDomain findUser = userService.getByStudentId(id);

        //then
        assertThat(findUser.getStudentId()).isEqualTo(user.getStudentId());
        assertThat(findUser.getEmail()).isEqualTo(user.getEmail());
        assertThat(findUser.getName()).isEqualTo(user.getName());
    }

    @Test
    @Tag("setupRequired")
    @DisplayName("존재하지 않는 사용자를 찾을때 예외 발생 테스트")
    void getByStudentIdUserNotFoundExceptionTest() {
        //given
        Long wrongId = 10000000L;

        //when & then
        assertThatThrownBy(() -> userService.getByStudentId(wrongId))
            .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @Tag("setupRequired")
    @DisplayName("회원 탈퇴 테스트")
    void withdrawalTest() {
        // when
        boolean result = userService.withdrawal(String.valueOf(id), password);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @Tag("setupRequired")
    @DisplayName("회원 탈퇴 시 비밀번호 불일치 테스트")
    void withdrawalInvalidPasswordTest() {
        // when
        boolean result = userService.withdrawal(String.valueOf(id), "wrong_password");

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("회원가입 시 검증 성공 테스트")
    void joinValidationSuccessTest() {
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
    @DisplayName("회원가입 시 비밀번호 불일치 테스트")
    void joinValidationInvalidPasswordTest() {
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
    @DisplayName("회원가입 시 학번 중복 테스트")
    void joinValidationDuplicateIdTest() {
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
    @DisplayName("회원가입 시 이메일 중복 테스트")
    void joinValidationDuplicateEmailTest() {
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
    @DisplayName("비밀번호 변경 성공 테스트")
    void changePasswordSuccessTest() {
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
    @DisplayName("비밀번호 변경 시 현재 패스워드 불일치 테스트")
    void changePasswordInvalidPasswordTest() {
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
    @DisplayName("비밀번호 변경시 새 패스워드 불일치 테스트")
    void changePasswordInvalidNewPasswordTest() {
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
