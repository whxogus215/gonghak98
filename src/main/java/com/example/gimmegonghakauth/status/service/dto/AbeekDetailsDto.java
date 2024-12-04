package com.example.gimmegonghakauth.status.service.dto;


import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AbeekDetailsDto {

    private ResultPointDto resultPoint;
    private List<CourseDetailsDto> coursesDetails;
}
