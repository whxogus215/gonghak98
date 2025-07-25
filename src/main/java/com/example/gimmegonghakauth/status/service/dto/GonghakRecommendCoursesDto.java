package com.example.gimmegonghakauth.status.service.dto;

import com.example.gimmegonghakauth.common.constant.AbeekTypeConst;
import java.util.List;
import java.util.Map;

public record GonghakRecommendCoursesDto(Map<AbeekTypeConst, List<IncompletedCoursesDto>> recommendCourses) {
}
