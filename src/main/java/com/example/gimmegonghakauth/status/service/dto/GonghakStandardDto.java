package com.example.gimmegonghakauth.status.service.dto;


import com.example.gimmegonghakauth.common.constant.AbeekTypeConst;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class GonghakStandardDto {

    private final Map<AbeekTypeConst, Integer> standards;
}
