package com.example.gimmegonghakauth.status.service.recommend;

import com.example.gimmegonghakauth.common.constant.AbeekTypeConst;
import com.example.gimmegonghakauth.common.constant.CourseCategoryConst;
import com.example.gimmegonghakauth.status.infrastructure.GonghakRepository;
import com.example.gimmegonghakauth.status.service.dto.GonghakRecommendCoursesDto;
import com.example.gimmegonghakauth.status.service.dto.GonghakStandardDto;
import com.example.gimmegonghakauth.status.service.dto.IncompletedCoursesDto;
import com.example.gimmegonghakauth.user.domain.UserDomain;
import java.util.ArrayList;
import java.util.Arrays;
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
        GonghakRecommendCoursesDto gonghakRecommendCoursesDto = new GonghakRecommendCoursesDto();

        // 수강하지 않은 과목 중 "전문 교양" 과목을 반환한다.
        List<IncompletedCoursesDto> professionalNonMajor = gonghakRepository.findUserIncompletedCourses(
            CourseCategoryConst.전문교양, user.getStudentId(), user.getMajorsDomain()
        );

        // 수강하지 않은 과목 중 "전공" 과목을 반환한다.
        List<IncompletedCoursesDto> major = gonghakRepository.findUserIncompletedCourses(
            CourseCategoryConst.전공, user.getStudentId(), user.getMajorsDomain()
        );

        // 수강하지 않은 과목 중 "MSC" 과목을 반환한다.
        List<IncompletedCoursesDto> msc = gonghakRepository.findUserIncompletedCourses(
            CourseCategoryConst.MSC, user.getStudentId(), user.getMajorsDomain()
        );

        // abeekType 별 추천 과목 List를 반환한다.
        Map<AbeekTypeConst, List<IncompletedCoursesDto>> coursesByAbeekTypeWithoutCompleteCourses = gonghakRecommendCoursesDto.getRecommendCoursesByAbeekType();
        Arrays.stream(AbeekTypeConst.values()).forEach(
            abeekType -> {
                List<IncompletedCoursesDto> abeekRecommend = new ArrayList<>();
                if (standard.getStandards().containsKey(abeekType)) {
                    switch (abeekType) {
                        case MSC:
                            abeekRecommend.addAll(msc);
                            break;
                        case MAJOR:
                            abeekRecommend.addAll(major);
                            break;
                        case DESIGN:
                            addOnlyDesignCreditOverZero(major, abeekRecommend);
                            break;
                        case PROFESSIONAL_NON_MAJOR:
                            abeekRecommend.addAll(professionalNonMajor);
                            break;
                        case NON_MAJOR:
                            abeekRecommend.addAll(professionalNonMajor);
                            break;
                        case MINIMUM_CERTI:
                            abeekRecommend.addAll(msc);
                            abeekRecommend.addAll(major);
                            abeekRecommend.addAll(professionalNonMajor);
                            break;
                    }
                    coursesByAbeekTypeWithoutCompleteCourses.put(abeekType, abeekRecommend);
                }

            }
        );

        return gonghakRecommendCoursesDto;
    }

    // 설계 과목(designCredit > 0)인 경우만 추가한다.
    private static void addOnlyDesignCreditOverZero(List<IncompletedCoursesDto> majorBasic,
                                                    List<IncompletedCoursesDto> abeekRecommend) {
        majorBasic.forEach(
            incompletedCoursesDto -> {
                if (incompletedCoursesDto.getDesignCredit() > 0) {
                    abeekRecommend.add(incompletedCoursesDto);
                }
            }
        );
    }

}
