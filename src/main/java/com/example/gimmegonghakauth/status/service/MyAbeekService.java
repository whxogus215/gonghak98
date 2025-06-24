package com.example.gimmegonghakauth.status.service;

import com.example.gimmegonghakauth.common.constant.AbeekTypeConst;
import com.example.gimmegonghakauth.common.domain.MajorsDomain;
import com.example.gimmegonghakauth.status.domain.Abeek;
import com.example.gimmegonghakauth.status.service.dto.CourseDetailsDto;
import com.example.gimmegonghakauth.status.service.dto.GonghakResultDto;
import com.example.gimmegonghakauth.status.service.dto.GonghakStandardDto;
import com.example.gimmegonghakauth.status.service.dto.IncompletedCoursesDto;
import com.example.gimmegonghakauth.status.service.recommend.GonghakRecommendService;
import com.example.gimmegonghakauth.status.service.recommend.RecommendServiceSelectManager;
import com.example.gimmegonghakauth.user.domain.UserDomain;
import com.example.gimmegonghakauth.user.service.UserService;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MyAbeekService {

    private final UserService userService;
    private final AbeekService abeekService;
    private final GonghakCoursesService gonghakCoursesService;
    private final RecommendServiceSelectManager recommendServiceSelectManager;

    public Optional<GonghakResultDto> getResult(Long studentId) {
        UserDomain user = userService.getByStudentId(studentId);
        MajorsDomain major = user.getMajorsDomain();

        // Abeek 기준
        GonghakStandardDto gonghakStandardDto = abeekService.findStandard(major).get();

        // 이수한 공학인증 과목
        List<CourseDetailsDto> completedCourse = gonghakCoursesService.findUserCompletedCourses(
            studentId, major);

        // 결과 반환
        Abeek abeek = new Abeek(gonghakStandardDto);
        return abeek.getResult(completedCourse);
    }

    public Map<AbeekTypeConst, List<IncompletedCoursesDto>> getRecommendResult(Long studentId) {
        GonghakRecommendService gonghakRecommendService = recommendServiceSelectManager.selectRecommendService(studentId);
        return gonghakRecommendService.createRecommendCourses(studentId)
                                      .getRecommendCoursesByAbeekType();
    }
}
