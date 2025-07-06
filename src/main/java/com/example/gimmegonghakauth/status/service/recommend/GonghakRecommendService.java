package com.example.gimmegonghakauth.status.service.recommend;

import com.example.gimmegonghakauth.status.service.dto.GonghakRecommendCoursesDto;
import com.example.gimmegonghakauth.status.service.dto.GonghakStandardDto;
import com.example.gimmegonghakauth.user.domain.UserDomain;

public interface GonghakRecommendService {
    GonghakRecommendCoursesDto createRecommendCourses(UserDomain userDomain, GonghakStandardDto standard);
}
