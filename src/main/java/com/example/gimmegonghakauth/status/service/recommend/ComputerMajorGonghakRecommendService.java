package com.example.gimmegonghakauth.status.service.recommend;


import static com.example.gimmegonghakauth.common.constant.CourseCategoryConst.BSM;
import static com.example.gimmegonghakauth.common.constant.CourseCategoryConst.전공;
import static com.example.gimmegonghakauth.common.constant.CourseCategoryConst.전문교양;

import com.example.gimmegonghakauth.common.constant.AbeekTypeConst;
import com.example.gimmegonghakauth.common.constant.CourseCategoryConst;
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
public class ComputerMajorGonghakRecommendService implements GonghakRecommendService {

    private final GonghakRepository gonghakRepository;

    @Override
    @Transactional(readOnly = true)
    public GonghakRecommendCoursesDto createRecommendCourses(UserDomain user, GonghakStandardDto standard) {
        List<CourseCategoryConst> courseCategories = Arrays.asList(전문교양, 전공, BSM);
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
        List<IncompletedCoursesDto> bsm = userIncompletedCourses.stream()
                                                                .filter(course -> course.getCourseCategory() == BSM)
                                                                .toList();

        standard.getStandards().keySet().forEach(
            abeekType -> {
                switch (abeekType) {
                    case BSM -> recommendCourses.get(abeekType).addAll(bsm);
                    case MAJOR -> recommendCourses.get(abeekType).addAll(major);
                    case PROFESSIONAL_NON_MAJOR, NON_MAJOR -> recommendCourses.get(abeekType).addAll(professionalNonMajor);
                    case MINIMUM_CERTI -> {
                        recommendCourses.get(abeekType).addAll(bsm);
                        recommendCourses.get(abeekType).addAll(major);
                        recommendCourses.get(abeekType).addAll(professionalNonMajor);
                    }
                    case DESIGN -> {
                        List<IncompletedCoursesDto> incompletedCourses = recommendCourses.get(abeekType);
                        major.stream()
                             .filter(course -> course.getDesignCredit() > 0)
                             .forEach(incompletedCourses::add);
                    }
                    default -> {
                    }
                }
            }
        );

        return new GonghakRecommendCoursesDto(recommendCourses);
    }
}
