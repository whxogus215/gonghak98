package com.example.gimmegonghakauth.status.service.dto;

import com.example.gimmegonghakauth.common.constant.AbeekTypeConst;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class GonghakResultDto {

    private final Map<AbeekTypeConst, AbeekDetailsDto> userResult;
}
