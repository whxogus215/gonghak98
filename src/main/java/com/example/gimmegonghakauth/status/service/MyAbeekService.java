package com.example.gimmegonghakauth.status.service;

import com.example.gimmegonghakauth.common.domain.MajorsDomain;
import com.example.gimmegonghakauth.status.domain.Abeek;
import com.example.gimmegonghakauth.status.service.dto.CourseDetailsDto;
import com.example.gimmegonghakauth.status.service.dto.GonghakRecommendCoursesDto;
import com.example.gimmegonghakauth.status.service.dto.GonghakResultDto;
import com.example.gimmegonghakauth.status.service.dto.GonghakStandardDto;
import com.example.gimmegonghakauth.status.service.dto.MyAbeekResponse;
import com.example.gimmegonghakauth.status.service.recommend.GonghakRecommendService;
import com.example.gimmegonghakauth.status.service.recommend.RecommendServiceSelectManager;
import com.example.gimmegonghakauth.user.domain.UserDomain;
import com.example.gimmegonghakauth.user.service.UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MyAbeekService {

    private final UserService userService;
    private final AbeekService abeekService;
    private final GonghakCoursesService gonghakCoursesService;
    private final RecommendServiceSelectManager recommendServiceSelectManager;

    @Transactional(readOnly = true)
    public MyAbeekResponse getUserResult(Long studentId) {
        UserDomain user = userService.getByStudentId(studentId);
        MajorsDomain major = user.getMajorsDomain();

        // 사용자 인증현황 조회
        GonghakStandardDto gonghakStandard = abeekService.findLatestStandardByMajor(major).orElseThrow(IllegalArgumentException::new);
        List<CourseDetailsDto> completedCourse = gonghakCoursesService.findUserCompletedCourses(studentId, major);
        Abeek abeek = new Abeek(gonghakStandard);
        GonghakResultDto gonghakResultDto = abeek.getResult(completedCourse).orElseThrow(IllegalArgumentException::new);

        // 사용자 인증현황에 따른 추천 과목 조회
        GonghakRecommendService gonghakRecommendService = recommendServiceSelectManager.selectRecommendService(major);
        GonghakRecommendCoursesDto recommendCourses = gonghakRecommendService.createRecommendCourses(user, gonghakStandard);

        return new MyAbeekResponse(gonghakResultDto, recommendCourses);
    }
}
