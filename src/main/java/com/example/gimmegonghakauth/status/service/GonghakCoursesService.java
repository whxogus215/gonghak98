package com.example.gimmegonghakauth.status.service;

import com.example.gimmegonghakauth.common.constant.CourseCategory;
import com.example.gimmegonghakauth.common.domain.MajorsDomain;
import com.example.gimmegonghakauth.status.infrastructure.GonghakCoursesRepository;
import com.example.gimmegonghakauth.status.service.dto.CourseDetailsDto;
import com.example.gimmegonghakauth.status.service.dto.IncompletedCoursesDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GonghakCoursesService {

    private static final int DIVIDER = 1000000;

    private final GonghakCoursesRepository gonghakCoursesRepository;

    public List<CourseDetailsDto> findUserCompletedCourses(
        Long studentId, MajorsDomain majorsDomain) {
        return gonghakCoursesRepository.findUserCompletedCourses(studentId, majorsDomain.getId(),
            studentId / DIVIDER);
    }

    // gonghakCourse 중 이수하지 않은 과목을 불러온다.
    public List<IncompletedCoursesDto> findUserIncompletedCourses(
        CourseCategory courseCategory, Long studentId, MajorsDomain majorsDomain) {
        return gonghakCoursesRepository.findUserIncompletedCourses(courseCategory, studentId,
            majorsDomain, studentId / DIVIDER);
    }
}
