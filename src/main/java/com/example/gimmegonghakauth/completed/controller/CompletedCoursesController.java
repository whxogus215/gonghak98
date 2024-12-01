package com.example.gimmegonghakauth.completed.controller;

import com.example.gimmegonghakauth.completed.domain.CompletedCoursesDomain;
import com.example.gimmegonghakauth.completed.service.CompletedCoursesService;
import com.example.gimmegonghakauth.file.service.exception.FileException;
import java.io.IOException;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class CompletedCoursesController {

    private final CompletedCoursesService completedCoursesService;

    public CompletedCoursesController(CompletedCoursesService completedCoursesService) {
        this.completedCoursesService = completedCoursesService;
    }

    @GetMapping("/excel")
    public String excel(Model model, Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Long studentId = Long.parseLong(userDetails.getUsername());
        List<CompletedCoursesDomain> datas = completedCoursesService.getCompletedCourses(studentId);
        model.addAttribute("datas", datas);
        return "excel/excelList";
    }

    @PostMapping("/excel/read")
    public String readExcel(@RequestParam("file") MultipartFile file, Model model, Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Long studentId = Long.parseLong(userDetails.getUsername());
        List<CompletedCoursesDomain> beforeDatas = completedCoursesService.getCompletedCourses(studentId);
        model.addAttribute("datas", beforeDatas);

        try {
            completedCoursesService.saveCompletedCourses(file, studentId);
            List<CompletedCoursesDomain> afterDatas = completedCoursesService.getCompletedCourses(studentId);
            model.addAttribute("datas", afterDatas);
            return "excel/excelList";
        } catch (IOException | FileException e) {
            model.addAttribute("error", e.getMessage());
            return "excel/excelList";
        }
    }
}
