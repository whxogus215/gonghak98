package com.example.gimmegonghakauth.status.service;

import com.example.gimmegonghakauth.abeek.service.AbeekService;
import com.example.gimmegonghakauth.completed.service.CompletedCoursesService;
import com.example.gimmegonghakauth.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StatusService {

    private final UserService userService;
    private final AbeekService abeekService;
    private final CompletedCoursesService completedCoursesService;

    // 반환타입, 메서드 이름은 임시로 지었음
    public void get(final Long studentId) {
        /**
         * 1. UserService에게 studentId를 전달해서 UserDomain을 요청한다.
         * 2. C.C Service에게 studentId와 MajorsDomain을 전달해서 기이수 공학인증 과목을 요청한다.
         * - (GonghakRepository 메서드 그대로 C.C Service에 넣기)
         * 3. AbeekService에게 studentId와 MajorsDomain을 전달해서 알맞는 UserAbeek를 요청한다.
         * 4. UserAbeek에게 기이수 공학인증 과목(List)을 전달해서 GonghakResultDto를 요청한다.
         * - 단, 각 하위 서비스별로 테스트 코드를 정교화해야된다.
         */
    }
}
