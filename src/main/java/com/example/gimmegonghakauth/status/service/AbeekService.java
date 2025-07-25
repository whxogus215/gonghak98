package com.example.gimmegonghakauth.status.service;

import com.example.gimmegonghakauth.common.constant.AbeekTypeConst;
import com.example.gimmegonghakauth.common.domain.MajorsDomain;
import com.example.gimmegonghakauth.status.domain.AbeekDomain;
import com.example.gimmegonghakauth.status.infrastructure.AbeekRepository;
import com.example.gimmegonghakauth.status.service.dto.GonghakStandardDto;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AbeekService {

    private static final int LATEST_YEAR = 25;

    private final AbeekRepository abeekRepository;

    public Optional<GonghakStandardDto> findLatestStandardByMajor(MajorsDomain majorsDomain){
        Map<AbeekTypeConst, Integer> standards = new ConcurrentHashMap<>();
        // year, major를 기준으로 abeek 데이터를 불러온다.
        List<AbeekDomain> allByYearAndMajorsDomain = abeekRepository.findAllByYearAndMajorsDomain(LATEST_YEAR, majorsDomain);

        // abeek을 기반으로 abeekType(영역별 구분),minCredit(영역별 인증학점) 저장한다.
        allByYearAndMajorsDomain.forEach(
            abeekDomain -> standards.put(abeekDomain.getAbeekType(),abeekDomain.getMinCredit())
        );

        return Optional.of(new GonghakStandardDto(standards));
    }
}
