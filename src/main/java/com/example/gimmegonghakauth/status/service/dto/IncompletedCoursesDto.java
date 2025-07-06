package com.example.gimmegonghakauth.status.service.dto;

import com.example.gimmegonghakauth.common.constant.CourseCategoryConst;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class IncompletedCoursesDto {
    private final String courseName;
    private final CourseCategoryConst courseCategory;
    private final int credit;
    private final double designCredit;
}
