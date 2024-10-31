package com.example.gimmegonghakauth.user.service.exception;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(Long studentId) {
        super(studentId + " 학생은 존재하지 않습니다.");
    }
}
