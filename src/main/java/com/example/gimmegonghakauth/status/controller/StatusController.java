package com.example.gimmegonghakauth.status.controller;

import com.example.gimmegonghakauth.common.constant.AbeekTypeConst;
import com.example.gimmegonghakauth.status.service.MyAbeekService;
import com.example.gimmegonghakauth.status.service.dto.AbeekDetailsDto;
import com.example.gimmegonghakauth.status.service.dto.CourseDetailsDto;
import com.example.gimmegonghakauth.status.service.dto.GonghakResultDto;
import com.example.gimmegonghakauth.status.service.dto.ResultPointDto;
import com.example.gimmegonghakauth.user.infrastructure.UserRepository;
import com.example.gimmegonghakauth.user.domain.UserDomain;
import com.example.gimmegonghakauth.status.service.dto.IncompletedCoursesDto;
import com.example.gimmegonghakauth.status.service.GonghakCalculateService;
import com.example.gimmegonghakauth.status.service.recommend.GonghakRecommendService;
import com.example.gimmegonghakauth.status.service.recommend.RecommendServiceSelectManager;
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

    //private final GonghakCalculateService gonghakCalculateService;
    private final MyAbeekService myAbeekService;
    private final RecommendServiceSelectManager recommendServiceSelectManager;
    private final UserRepository userRepository;

    // 사용자의 공학인증 현황과 추천 과목을 가져온다.
    @GetMapping("/status")
    public String readGonghakStatusResult(Authentication authentication, Model model) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Long studentId = Long.parseLong(userDetails.getUsername());

        // 컨트롤러가 UserDomain 객체를 가져오는 역할을 수행하고 있음.
        UserDomain student = userRepository.findByStudentId(studentId).get();

        readUserResult(model, studentId);
        readUserRecommendCourses(model, studentId, student);
        return "gonghak/statusForm";
    }

    // 사용자의 인증 현황 데이터를 가져온다.
    private void readUserResult(Model model, Long studentId) {
        GonghakResultDto userResultRatio = myAbeekService.getResult(
                studentId)
            .orElseThrow(IllegalArgumentException::new);
        addResultPoint(model, userResultRatio);
        addCoursesDetails(model, userResultRatio);
    }

    private void addResultPoint(Model model, GonghakResultDto result){
        Map<AbeekTypeConst, AbeekDetailsDto> userResult = result.getUserResult();
        Map<AbeekTypeConst, ResultPointDto> resultPoint = new ConcurrentHashMap<>();
        userResult.forEach((abeekTypeConst, abeekDetailsDto) -> {
            resultPoint.put(abeekTypeConst, abeekDetailsDto.getResultPoint());
        });

        model.addAttribute("userResultRatio", resultPoint);
    }

    private void addCoursesDetails(Model model, GonghakResultDto result){
        Map<AbeekTypeConst, AbeekDetailsDto> userResult = result.getUserResult();

        Map<AbeekTypeConst, List<CourseDetailsDto>> coursesDetails = new ConcurrentHashMap<>();
        userResult.forEach((abeekTypeConst, abeekDetailsDto) -> {
            coursesDetails.put(abeekTypeConst, abeekDetailsDto.getCoursesDetails());
        });

        model.addAttribute("userCourseDetails", coursesDetails);
    }


    // 사용자의 공학인증 추천 과목을 가져온다.
    private void readUserRecommendCourses(Model model, Long studentId, UserDomain student) {
        GonghakRecommendService gonghakRecommendService = recommendServiceSelectManager.selectRecommendService(
            studentId);
        Map<AbeekTypeConst, List<IncompletedCoursesDto>> recommendCoursesByAbeekType =
            gonghakRecommendService.createRecommendCourses(student)
                .getRecommendCoursesByAbeekType();
        model.addAttribute("recommendCoursesByAbeekType", recommendCoursesByAbeekType);
    }

}
