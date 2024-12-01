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

    public UserDomain create(String _studentId, String password, String email,
        MajorsDomain majorsDomain, String name) {
        Long studentId = Long.parseLong(_studentId);
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

        UserDomain user = findUserByStudentId(studentId);

        if (!isPasswordValid(user, password)) {
            return false;
        }

        deleteAssociatedCourses(user);
        deleteUser(user);

        return true;
    }

    private UserDomain findUserByStudentId(Long studentId) {
        return userRepository.findByStudentId(studentId)
            .orElseThrow(() -> new UsernameNotFoundException("학번이 존재하지 않습니다."));
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
        String password = user.getPassword();
        String inputPassword = changePasswordDto.getCurrentPassword();
        if (!userEncoder.matches(inputPassword, password)) { //입력한 패스워드가 현재 패스워드와 일치하지 않을 경우
            bindingResult.rejectValue("currentPassword", "currentPasswordInCorrect",
                "현재 패스워드가 일치하지 않습니다.");
            return false;
        }
        if (userEncoder.matches(changePasswordDto.getNewPassword1(),
            password)) { //입력한 새 패스워드가 현재 패스워드와 일치하는 경우
            bindingResult.rejectValue("newPassword1", "sameCurrentPassword",
                "현재 패스워드와 다른 패스워드를 입력해주세요.");
            return false;
        }
        if (!changePasswordDto.getNewPassword1()
            .equals(changePasswordDto.getNewPassword2())) {//새 패스워드 2개의 입력이 일치하지 않는 경우
            bindingResult.rejectValue("newPassword2", "newPasswordInCorrect",
                "입력한 패스워드가 일치하지 않습니다.");
            return false;
        }
        return true;
    }
}
