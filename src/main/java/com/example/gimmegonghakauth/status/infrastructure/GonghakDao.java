package com.example.gimmegonghakauth.status.infrastructure;

import com.example.gimmegonghakauth.common.constant.AbeekTypeConst;
import com.example.gimmegonghakauth.common.constant.CourseCategory;
import com.example.gimmegonghakauth.status.domain.AbeekDomain;
import com.example.gimmegonghakauth.common.domain.MajorsDomain;
import com.example.gimmegonghakauth.status.service.dto.CourseDetailsDto;
import com.example.gimmegonghakauth.status.service.dto.GonghakStandardDto;
import com.example.gimmegonghakauth.status.service.dto.IncompletedCoursesDto;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Repository
@Transactional
@RequiredArgsConstructor
public class GonghakDao implements GonghakRepository{

    private static final int DIVIDER = 1000000;
    private static final int LATEST_YEAR = 24;
    private final AbeekDao abeekDao;
    private final GonghakCoursesDao gonghakCoursesDao;

    @Override
    public AbeekDomain save(AbeekDomain abeekDomain) {
        abeekDao.save(abeekDomain);
        return abeekDomain;
    }

    // 최신년도의 abeekType(영역별 구분),minCredit(영역별 인증학점) 불러온다.
    @Override
    public Optional<GonghakStandardDto> findStandard(MajorsDomain majorsDomain){
        return changeToGonghakStandardDto(majorsDomain, LATEST_YEAR);
    }

    // gonghakCourse 중 이수한 과목을 불러온다.
    @Override
    public List<CourseDetailsDto> findUserCompletedCourses(
        Long studentId, MajorsDomain majorsDomain) {
        return gonghakCoursesDao.findUserCompletedCourses(studentId,majorsDomain.getId(), studentId/DIVIDER);
    }

    // gonghakCourse 중 이수하지 않은 과목을 불러온다.
    @Override
    public List<IncompletedCoursesDto> findUserIncompletedCourses(List<CourseCategory> courseCategories,
                                                                  Long studentId,
                                                                  MajorsDomain majorsDomain) {
        return gonghakCoursesDao.findUserIncompletedCourses(courseCategories, studentId, majorsDomain, studentId/DIVIDER);
    }

    private Optional<GonghakStandardDto> changeToGonghakStandardDto(MajorsDomain majorsDomain, int year) {

        Map<AbeekTypeConst, Integer> standards = new ConcurrentHashMap<>();
        // year, major를 기준으로 abeek 데이터를 불러온다.
        List<AbeekDomain> allByYearAndMajorsDomain = abeekDao.findAllByYearAndMajorsDomain(year, majorsDomain);

        // abeek을 기반으로 abeekType(영역별 구분),minCredit(영역별 인증학점) 저장한다.
        allByYearAndMajorsDomain.forEach(
            abeekDomain -> standards.put(abeekDomain.getAbeekType(),abeekDomain.getMinCredit()));
        log.info("standards={}",standards);

        return Optional.of(new GonghakStandardDto(standards));
    }

}
