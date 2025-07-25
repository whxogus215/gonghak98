package com.example.gimmegonghakauth.status.infrastructure;

import com.example.gimmegonghakauth.common.constant.CourseCategory;
import com.example.gimmegonghakauth.common.domain.MajorsDomain;
import com.example.gimmegonghakauth.status.domain.AbeekDomain;
import com.example.gimmegonghakauth.status.service.dto.CourseDetailsDto;
import com.example.gimmegonghakauth.status.service.dto.GonghakStandardDto;
import com.example.gimmegonghakauth.status.service.dto.IncompletedCoursesDto;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface GonghakRepository {

    AbeekDomain save(AbeekDomain abeekDomain);

    Optional<GonghakStandardDto> findStandard(MajorsDomain majorsDomain);

    List<CourseDetailsDto> findUserCompletedCourses(Long studentId, MajorsDomain majorsDomain);

    List<IncompletedCoursesDto> findUserIncompletedCourses(List<CourseCategory> courseCategories, Long studentId, MajorsDomain majorsDomain);
}
