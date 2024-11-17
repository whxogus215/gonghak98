package com.example.gimmegonghakauth.status.service.dto;

import com.example.gimmegonghakauth.common.constant.CourseCategoryConst;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CourseDetailsDto {

    private final Long courseId;
    private final String courseName;
    private final int year;
    private final String semester;
    private final CourseCategoryConst courseCategory;
    private final String passCategory;
    private final double designCredit;
    private final int credit;
}
