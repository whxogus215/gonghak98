package com.example.gimmegonghakauth.user.service;

import com.example.gimmegonghakauth.common.domain.MajorsDomain;
import com.example.gimmegonghakauth.completed.domain.CompletedCoursesDomain;
import com.example.gimmegonghakauth.completed.infrastructure.CompletedCoursesDao;
import com.example.gimmegonghakauth.user.domain.UserDomain;
import com.example.gimmegonghakauth.user.infrastructure.UserRepository;
import com.example.gimmegonghakauth.user.service.dto.ChangePasswordDto;
import com.example.gimmegonghakauth.user.service.dto.UserJoinDto;
import com.example.gimmegonghakauth.user.service.exception.UserNotFoundException;
import com.example.gimmegonghakauth.user.service.port.UserEncoder;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

@Transactional
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final CompletedCoursesDao completedCoursesDao;
    private final UserEncoder userEncoder;

    public UserDomain create(String id, String password, String email,
        MajorsDomain majorsDomain, String name) {
        Long studentId = Long.parseLong(id);
        UserDomain user = UserDomain.builder()
            .studentId(studentId).password(userEncoder.encode(password))
            .email(email).majorsDomain(majorsDomain).name(name)
            .build();
        userRepository.save(user);
        return user;
    }

    public UserDomain updatePassword(UserDomain user, String newPassword) {
        user.updatePassword(userEncoder.encode(newPassword));
        userRepository.save(user);
        return user;
    }

    public UserDomain getByStudentId(Long studentId) {
        return userRepository.findByStudentId(studentId)
            .orElseThrow(() -> new UserNotFoundException(studentId));
    }

    public boolean joinValidation(UserJoinDto userJoinDto, BindingResult bindingResult) {
        if (isPasswordMismatch(userJoinDto, bindingResult)) {
            return false;
        }

        if (isStudentIdDuplicate(userJoinDto.getStudentId(), bindingResult)) {
            return false;
        }

        if (isEmailDuplicate(userJoinDto.getEmail(), bindingResult)) {
            return false;
        }

        return true;
    }

    private boolean isPasswordMismatch(UserJoinDto userJoinDto, BindingResult bindingResult) {
        boolean mismatch = !userJoinDto.getPassword1().equals(userJoinDto.getPassword2());
        if (mismatch) {
            bindingResult.rejectValue("password2", "passwordInCorrect", "2개의 패스워드가 일치하지 않습니다.");
        }
        return mismatch;
    }

    private boolean isStudentIdDuplicate(String studentId, BindingResult bindingResult) {
        boolean duplicate = userRepository.existsByStudentId(Long.parseLong(studentId));
        if (duplicate) {
            bindingResult.rejectValue("studentId", "duplicate", "이미 등록된 학번입니다.");
        }
        return duplicate;
    }

    private boolean isEmailDuplicate(String email, BindingResult bindingResult) {
        boolean duplicate = userRepository.existsByEmail(email);
        if (duplicate) {
            bindingResult.rejectValue("email", "duplicate", "이미 등록된 이메일입니다.");
        }
        return duplicate;
    }
    
    public boolean withdrawal(String _studentId, String password) {
        Long studentId = Long.parseLong(_studentId);

        UserDomain user = getByStudentId(studentId);

        if (!isPasswordValid(user, password)) {
            return false;
        }

        deleteAssociatedCourses(user);
        deleteUser(user);

        return true;
    }

    private boolean isPasswordValid(UserDomain user, String password) {
        return userEncoder.matches(password, user.getPassword());
    }

    private void deleteAssociatedCourses(UserDomain user) {
        List<CompletedCoursesDomain> coursesList = completedCoursesDao.findByUserDomain(user);
        if (!coursesList.isEmpty()) {
            completedCoursesDao.deleteAllInBatch(coursesList);
        }
    }

    private void deleteUser(UserDomain user) {
        userRepository.delete(user);
    }

    public boolean changePasswordValidation(ChangePasswordDto changePasswordDto,
        BindingResult bindingResult, UserDomain user) {

        if (isCurrentPasswordInvalid(changePasswordDto.getCurrentPassword(), user.getPassword(),
            bindingResult)) {
            return false;
        }
        if (isNewPasswordSameAsCurrent(changePasswordDto.getNewPassword1(), user.getPassword(),
            bindingResult)) {
            return false;
        }
        if (isNewPasswordMismatch(changePasswordDto.getNewPassword1(),
            changePasswordDto.getNewPassword2(), bindingResult)) {
            return false;
        }

        return true;
    }

    private boolean isCurrentPasswordInvalid(String inputPassword, String currentPassword,
        BindingResult bindingResult) {
        if (!userEncoder.matches(inputPassword, currentPassword)) {
            bindingResult.rejectValue("currentPassword", "currentPasswordInCorrect",
                "현재 패스워드가 일치하지 않습니다.");
            return true;
        }
        return false;
    }

    private boolean isNewPasswordSameAsCurrent(String newPassword, String currentPassword,
        BindingResult bindingResult) {
        if (userEncoder.matches(newPassword, currentPassword)) {
            bindingResult.rejectValue("newPassword1", "sameCurrentPassword",
                "현재 패스워드와 다른 패스워드를 입력해주세요.");
            return true;
        }
        return false;
    }

    private boolean isNewPasswordMismatch(String newPassword1, String newPassword2,
        BindingResult bindingResult) {
        if (!newPassword1.equals(newPassword2)) {
            bindingResult.rejectValue("newPassword2", "newPasswordInCorrect",
                "입력한 패스워드가 일치하지 않습니다.");
            return true;
        }
        return false;
    }

}
