package com.example.gimmegonghakauth.status.service.recommend;

import static com.example.gimmegonghakauth.common.constant.CourseCategory.MSC;
import static com.example.gimmegonghakauth.common.constant.CourseCategory.전공;
import static com.example.gimmegonghakauth.common.constant.CourseCategory.전문교양;

import com.example.gimmegonghakauth.common.constant.AbeekTypeConst;
import com.example.gimmegonghakauth.common.constant.CourseCategory;
import com.example.gimmegonghakauth.status.infrastructure.GonghakRepository;
import com.example.gimmegonghakauth.status.service.dto.GonghakRecommendCoursesDto;
import com.example.gimmegonghakauth.status.service.dto.GonghakStandardDto;
import com.example.gimmegonghakauth.status.service.dto.IncompletedCoursesDto;
import com.example.gimmegonghakauth.user.domain.UserDomain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ElecInfoMajorGonghakRecommendService implements GonghakRecommendService {

    private final GonghakRepository gonghakRepository;

    @Transactional(readOnly = true)
    @Override
    public GonghakRecommendCoursesDto createRecommendCourses(UserDomain user, GonghakStandardDto standard) {
        List<CourseCategory> courseCategories = Arrays.asList(전문교양, 전공, MSC);
        List<IncompletedCoursesDto> userIncompletedCourses = gonghakRepository.findUserIncompletedCourses(courseCategories,
                                                                                                          user.getStudentId(),
                                                                                                          user.getMajorsDomain());

        Map<AbeekTypeConst, List<IncompletedCoursesDto>> recommendCourses = new EnumMap<>(AbeekTypeConst.class);
        standard.getStandards().keySet().forEach(
            abeekType -> recommendCourses.put(abeekType, new ArrayList<>())
        );

        List<IncompletedCoursesDto> professionalNonMajor = userIncompletedCourses.stream()
                                                                                 .filter(course -> course.getCourseCategory() == 전문교양)
                                                                                 .toList();
        List<IncompletedCoursesDto> major = userIncompletedCourses.stream()
                                                                  .filter(course -> course.getCourseCategory() == 전공)
                                                                  .toList();
        List<IncompletedCoursesDto> msc = userIncompletedCourses.stream()
                                                                .filter(course -> course.getCourseCategory() == MSC)
                                                                .toList();

        standard.getStandards().keySet().forEach(
            abeekType -> {
                switch (abeekType) {
                    case MSC -> recommendCourses.get(abeekType).addAll(msc);
                    case MAJOR -> recommendCourses.get(abeekType).addAll(major);
                    case PROFESSIONAL_NON_MAJOR, NON_MAJOR -> recommendCourses.get(abeekType).addAll(professionalNonMajor);
                    case MINIMUM_CERTI -> {
                        recommendCourses.get(abeekType).addAll(msc);
                        recommendCourses.get(abeekType).addAll(major);
                        recommendCourses.get(abeekType).addAll(professionalNonMajor);
                    }
                    case DESIGN -> {
                        List<IncompletedCoursesDto> incompletedCourses = recommendCourses.get(abeekType);
                        major.stream()
                             .filter(course -> course.getDesignCredit() > 0)
                             .forEach(incompletedCourses::add);
                    }
                    default -> throw new IllegalStateException("올바르지 않은 타입 : " + abeekType);
                }
            }
        );

        return new GonghakRecommendCoursesDto(recommendCourses);
    }
}
