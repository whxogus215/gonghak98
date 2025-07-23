package com.example.gimmegonghakauth.status.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.gimmegonghakauth.common.constant.AbeekTypeConst;
import com.example.gimmegonghakauth.common.domain.CoursesDomain;
import com.example.gimmegonghakauth.common.domain.MajorsDomain;
import com.example.gimmegonghakauth.common.infrastructure.CoursesDao;
import com.example.gimmegonghakauth.common.infrastructure.MajorsDao;
import com.example.gimmegonghakauth.completed.domain.CompletedCoursesDomain;
import com.example.gimmegonghakauth.completed.infrastructure.CompletedCoursesDao;
import com.example.gimmegonghakauth.status.service.dto.MyAbeekResponse;
import com.example.gimmegonghakauth.status.service.dto.ResultPointDto;
import com.example.gimmegonghakauth.user.domain.UserDomain;
import com.example.gimmegonghakauth.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class MyAbeekServiceTest {

    @Autowired
    private MyAbeekService myAbeekService;

    @Autowired
    private UserService userService;

    @Autowired
    private CompletedCoursesDao completedCoursesDao;

    @Autowired
    private CoursesDao coursesDao;

    @Autowired
    private MajorsDao majorsDao;

    @DisplayName("전자정보통신공학과 재학생은 이수한 ABEEK 영역별 진행도를 확인할 수 있다.")
    @ParameterizedTest
    @CsvSource({
        "9067, 전문교양, 3.0",  // 1. '문제해결을위한글쓰기와발표'(전문교양) 과목 하나만 이수
        "1357, MSC, 3.0",       // 2. '미적분학1'(MSC) 과목 하나만 이수
        "4268, 전공, 3.0"       // 3. '데이터구조론'(전공) 과목 하나만 이수
    })
    void readABEEKProgressTest(Long courseId, String abeekName, Double courseCredit) {
        // given
        AbeekTypeConst findAbeekType = AbeekTypeConst.getCourseCategoryType(abeekName);
        UserDomain user = createTestUserWithSingleCompletedCourse(courseId);

        // when
        MyAbeekResponse result = myAbeekService.getUserResult(user.getStudentId());

        // then
        ResultPointDto userResult = result.gonghakResultDto().getUserResult().get(findAbeekType).getResultPoint();
        assertThat(userResult.getUserPoint()).isEqualTo(courseCredit);
    }

    @Test
    @DisplayName("전자정보통신공학과 재학생은 인증에 필요한 추천과목을 확인할 수 있다.")
    void readRecommendCoursesTest() {
        // given
        // when
        //then
    }

    private UserDomain createTestUserWithSingleCompletedCourse(Long courseId) {
        int year = 25;
        String semester = "1학기";
        Long studentId = 25010693L;
        String password = "testPassword";
        String email = "test@university.ac.kr";
        String name = "김공학";

        MajorsDomain major = majorsDao.findById(1L).orElse(new MajorsDomain());
        UserDomain user = userService.create(String.valueOf(studentId), password, email, major, name);
        CoursesDomain course = coursesDao.findByCourseId(courseId);

        CompletedCoursesDomain completedCourse = CompletedCoursesDomain.builder()
                                                                       .userDomain(user)
                                                                       .coursesDomain(course)
                                                                       .year(year)
                                                                       .semester(semester)
                                                                       .build();
        completedCoursesDao.save(completedCourse);
        return user;
    }
}
