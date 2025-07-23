package com.example.gimmegonghakauth.status.infrastructure;

import com.example.gimmegonghakauth.common.constant.CourseCategory;
import com.example.gimmegonghakauth.common.domain.MajorsDomain;
import com.example.gimmegonghakauth.status.domain.GonghakCoursesDomain;
import com.example.gimmegonghakauth.status.service.dto.CourseDetailsDto;
import com.example.gimmegonghakauth.status.service.dto.IncompletedCoursesDto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GonghakCoursesRepository extends JpaRepository<GonghakCoursesDomain,Long> {

    @Query("select new com.example.gimmegonghakauth.status.service.dto.CourseDetailsDto(GCD.courseDomain.courseId, GCD.courseDomain.name, CCD.year, CCD.semester, GCD.courseCategory, GCD.passCategory, GCD.designCredit, GCD.courseDomain.credit) from GonghakCoursesDomain GCD "
        + "join CompletedCoursesDomain CCD on GCD.courseDomain = CCD.courseDomain "
        + "where CCD.userDomain.studentId =:studentId and GCD.majorsDomain.id = :majorsId and GCD.year = :year")
    List<CourseDetailsDto> findUserCompletedCourses(@Param("studentId") Long studentId, @Param("majorsId") Long majorId, @Param("year") Long year);

    @Query("select new com.example.gimmegonghakauth.status.service.dto.IncompletedCoursesDto(GCD.courseDomain.name, GCD.courseCategory, GCD.courseDomain.credit, GCD.designCredit) from GonghakCoursesDomain GCD  "
        + "left join CompletedCoursesDomain CCD on CCD.courseDomain = GCD.courseDomain "
        + "where GCD.majorsDomain = :majorsDomain and GCD.year = :year and GCD.courseCategory = :courseCategory and CCD.id is null and :studentId is not null")
    List<IncompletedCoursesDto> findUserIncompletedCourses(@Param("courseCategory") CourseCategory courseCategory, @Param("studentId") Long studentId, @Param("majorsDomain") MajorsDomain majorsDomain, @Param("year") Long year);
}


