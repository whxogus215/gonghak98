package com.example.gimmegonghakauth.status.service;

import com.example.gimmegonghakauth.common.domain.MajorsDomain;
import com.example.gimmegonghakauth.status.infrastructure.GonghakRepository;
import com.example.gimmegonghakauth.status.service.dto.CourseDetailsDto;
import com.example.gimmegonghakauth.status.service.dto.GonghakStandardDto;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AbeekService {

    private final GonghakRepository gonghakRepository;

    public Optional<GonghakStandardDto> getStandard(MajorsDomain majorsDomain) {
        return gonghakRepository.findStandard(majorsDomain);
    }

    public List<CourseDetailsDto> findUserCompletedCourses(Long studentId, MajorsDomain majorsDomain){
        return gonghakRepository.findUserCompletedCourses(studentId,majorsDomain);
    }
}
