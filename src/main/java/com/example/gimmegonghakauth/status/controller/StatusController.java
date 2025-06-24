package com.example.gimmegonghakauth.status.controller;

import com.example.gimmegonghakauth.common.constant.AbeekTypeConst;
import com.example.gimmegonghakauth.status.service.MyAbeekService;
import com.example.gimmegonghakauth.status.service.dto.AbeekDetailsDto;
import com.example.gimmegonghakauth.status.service.dto.CourseDetailsDto;
import com.example.gimmegonghakauth.status.service.dto.GonghakResultDto;
import com.example.gimmegonghakauth.status.service.dto.IncompletedCoursesDto;
import com.example.gimmegonghakauth.status.service.dto.ResultPointDto;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/gonghak")
@Slf4j
@RequiredArgsConstructor
public class StatusController {

    private final MyAbeekService myAbeekService;

    // 사용자의 공학인증 현황과 추천 과목을 가져온다.
    @GetMapping("/status")
    public String readGonghakStatusResult(Authentication authentication, Model model) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Long studentId = Long.parseLong(userDetails.getUsername());

        log.info("사용자 인증현황=====================");
        GonghakResultDto userResultRatio = myAbeekService.getResult(studentId)
                                                         .orElseThrow(IllegalArgumentException::new);
        addResultPoint(model, userResultRatio);
        addCoursesDetails(model, userResultRatio);
        log.info("사용자 인증현황=====================");

        log.info("사용자 추천과목=====================");
        Map<AbeekTypeConst, List<IncompletedCoursesDto>> recommendCoursesByAbeekType = myAbeekService.getRecommendResult(studentId);
        model.addAttribute("recommendCoursesByAbeekType", recommendCoursesByAbeekType);
        log.info("사용자 추천과목=====================");

        return "gonghak/statusForm";
    }

    private void addResultPoint(Model model, GonghakResultDto result) {
        Map<AbeekTypeConst, AbeekDetailsDto> userResult = result.getUserResult();
        Map<AbeekTypeConst, ResultPointDto> resultPoint = new ConcurrentHashMap<>();
        userResult.forEach((abeekTypeConst, abeekDetailsDto) -> resultPoint.put(abeekTypeConst, abeekDetailsDto.getResultPoint()));

        model.addAttribute("userResultRatio", resultPoint);
    }

    private void addCoursesDetails(Model model, GonghakResultDto result) {
        Map<AbeekTypeConst, AbeekDetailsDto> userResult = result.getUserResult();

        Map<AbeekTypeConst, List<CourseDetailsDto>> coursesDetails = new ConcurrentHashMap<>();
        userResult.forEach((abeekTypeConst, abeekDetailsDto) -> coursesDetails.put(abeekTypeConst, abeekDetailsDto.getCoursesDetails()));

        model.addAttribute("userCourseDetails", coursesDetails);
    }
}
