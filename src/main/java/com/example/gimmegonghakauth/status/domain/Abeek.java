package com.example.gimmegonghakauth.status.domain;

import com.example.gimmegonghakauth.common.constant.AbeekTypeConst;
import com.example.gimmegonghakauth.status.service.dto.AbeekDetailsDto;
import com.example.gimmegonghakauth.status.service.dto.CourseDetailsDto;
import com.example.gimmegonghakauth.status.service.dto.GonghakResultDto;
import com.example.gimmegonghakauth.status.service.dto.GonghakStandardDto;
import com.example.gimmegonghakauth.status.service.dto.ResultPointDto;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class Abeek {

    private final Map<AbeekTypeConst, Integer> standards;

    public Abeek(GonghakStandardDto gonghakStandardDto) {
        this.standards = gonghakStandardDto.getStandards();
    }

    public Optional<GonghakResultDto> getResult(List<CourseDetailsDto> userCompletedCourses) {
        // default user abeek 학점 상태 map
        Map<AbeekTypeConst, AbeekDetailsDto> userResult = getUserAbeekCreditDefault(standards);

        // stackUserGonghakCredit -> abeekType에 맞게 이수한 총 학점을 계산한다.
        stackUserGonghakCredit(userCompletedCourses, userResult);

        // 인증 상태(비율) return
        return Optional.of(new GonghakResultDto(userResult));
    }

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

        if (userResult.containsKey(AbeekTypeConst.NON_MAJOR)
            && abeekTypeConst == AbeekTypeConst.PROFESSIONAL_NON_MAJOR) {
            abeekTypeConst = AbeekTypeConst.NON_MAJOR;
        }

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
        CourseDetailsDto courseDetailsDto, Map<AbeekTypeConst, AbeekDetailsDto> userAbeekCredit) {
        if (getInputCredit(abeekTypeConst, courseDetailsDto) == 0) {
            return;
        }

        if (userAbeekCredit.containsKey(AbeekTypeConst.NON_MAJOR)
            && abeekTypeConst == AbeekTypeConst.PROFESSIONAL_NON_MAJOR) {
            abeekTypeConst = AbeekTypeConst.NON_MAJOR;
        }

        AbeekDetailsDto currentDetails = userAbeekCredit.get(abeekTypeConst);
        if (currentDetails != null) {
            List<CourseDetailsDto> updatedCourses = currentDetails.getCoursesDetails();
            updatedCourses.add(courseDetailsDto);
        }
    }
}
