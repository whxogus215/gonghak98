package com.example.gimmegonghakauth.status.service;

import com.example.gimmegonghakauth.common.constant.AbeekTypeConst;
import com.example.gimmegonghakauth.status.infrastructure.GonghakRepository;
import com.example.gimmegonghakauth.status.service.dto.AbeekDetailsDto;
import com.example.gimmegonghakauth.status.service.dto.CourseDetailsDto;
import com.example.gimmegonghakauth.status.service.dto.GonghakResultDto;
import com.example.gimmegonghakauth.status.service.dto.GonghakStandardDto;
import com.example.gimmegonghakauth.status.service.dto.ResultPointDto;
import com.example.gimmegonghakauth.user.domain.UserDomain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GonghakCalculateService {

    private final GonghakRepository gonghakRepository;

    @Transactional(readOnly = true)
    public Optional<GonghakResultDto> getResult(UserDomain userDomain) {

        // findStandard -> 학번 입학년도를 기준으로 해당 년도의 abeekType(영역별 구분),minCredit(영역별 인증학점) 불러온다.
        Optional<GonghakStandardDto> standard = gonghakRepository.findStandard(
            userDomain.getMajorsDomain());

        // default user abeek 학점 상태 map
        Map<AbeekTypeConst, AbeekDetailsDto> userResult = getUserAbeekCreditDefault(
            standard.get().getStandards());

        // user 공학 상태 테이블
        // gonghakCourse 중 이수한 과목을 불러온다.
        List<CourseDetailsDto> userCompletedCourses = gonghakRepository.findUserCompletedCourses(
            userDomain.getStudentId(), userDomain.getMajorsDomain());

        // user
        // stackUserGonghakCredit -> abeekType에 맞게 이수한 총 학점을 계산한다.
        stackUserGonghakCredit(userCompletedCourses,
            userResult);

        // 인증 상태(비율) return
        return Optional.of(new GonghakResultDto(userResult));
    }


    // default user abeek 학점 상태 map을 만들어 반환한다.
    private Map<AbeekTypeConst, AbeekDetailsDto> getUserAbeekCreditDefault(
        Map<AbeekTypeConst, Integer> standards) {
        Map<AbeekTypeConst, AbeekDetailsDto> userAbeekCredit = new ConcurrentHashMap<>();
        Arrays.stream(AbeekTypeConst.values()).forEach(abeekTypeConst -> {
            if (standards.containsKey(abeekTypeConst)) {
                ResultPointDto resultPoint = new ResultPointDto(0.0, standards.get(abeekTypeConst));
                AbeekDetailsDto abeekDetailsDto = new AbeekDetailsDto(resultPoint,
                    new ArrayList<>());
                userAbeekCredit.put(abeekTypeConst, abeekDetailsDto);
            }
        });
        return userAbeekCredit;
    }


    // abeekType에 맞게 이수한 총 학점을 계산한다.
    private void stackUserGonghakCredit(List<CourseDetailsDto> userCoursesByMajor,
        Map<AbeekTypeConst, AbeekDetailsDto> userResult) {
        userCoursesByMajor.forEach(courseDetailsDto -> {
            AbeekTypeConst typeConst = getCourseCategoryType(
                String.valueOf(courseDetailsDto.getCourseCategory()));
            if (typeConst != null) {
                stackCredit(typeConst, courseDetailsDto, userResult);
                addCourseToDetails(typeConst, courseDetailsDto, userResult);
            }
            stackCredit(AbeekTypeConst.DESIGN, courseDetailsDto, userResult);
            addCourseToDetails(AbeekTypeConst.DESIGN, courseDetailsDto, userResult);
            stackCredit(AbeekTypeConst.MINIMUM_CERTI, courseDetailsDto, userResult);
            addCourseToDetails(AbeekTypeConst.MINIMUM_CERTI, courseDetailsDto, userResult);
        });
    }

    private AbeekTypeConst getCourseCategoryType(String courseCategory) {
        switch (courseCategory) {
            case "전공":
                return AbeekTypeConst.MAJOR;
            case "전문교양":
                return AbeekTypeConst.PROFESSIONAL_NON_MAJOR;
            case "교양":
                return AbeekTypeConst.NON_MAJOR;
            case "MSC":
                return AbeekTypeConst.MSC;
            case "BSM":
                return AbeekTypeConst.BSM;
            default:
                return null;
        }
    }


    private void stackCredit(AbeekTypeConst abeekTypeConst, CourseDetailsDto courseDetailsDto,
        Map<AbeekTypeConst, AbeekDetailsDto> userResult) {
        double inputCredit = getInputCredit(abeekTypeConst, courseDetailsDto);
        AbeekDetailsDto currentDetails = userResult.get(abeekTypeConst);
        if (currentDetails != null) {
            ResultPointDto currentResultPoint = currentDetails.getResultPoint();
            double newUserPoint = currentResultPoint.getUserPoint() + inputCredit;

            currentResultPoint.setUserPoint(newUserPoint);
        }
    }


    private double getInputCredit(AbeekTypeConst abeekTypeConst,
        CourseDetailsDto courseDetailsDto) {
        return (abeekTypeConst == AbeekTypeConst.DESIGN) ? courseDetailsDto.getDesignCredit()
            : (double) courseDetailsDto.getCredit();
    }

    private void addCourseToDetails(AbeekTypeConst abeekTypeConst,
        CourseDetailsDto courseDetailsDto,
        Map<AbeekTypeConst, AbeekDetailsDto> userAbeekCredit) {
        if (getInputCredit(abeekTypeConst, courseDetailsDto) == 0) {
            return;
        }

        AbeekDetailsDto currentDetails = userAbeekCredit.get(abeekTypeConst);
        if (currentDetails != null) {
            List<CourseDetailsDto> updatedCourses = currentDetails.getCoursesDetails();
            updatedCourses.add(courseDetailsDto);
        }
    }


}
