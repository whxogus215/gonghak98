package com.example.gimmegonghakauth.status.service.recommend;

import static com.example.gimmegonghakauth.status.domain.MajorName.ELEC_INFO;

import com.example.gimmegonghakauth.common.domain.MajorsDomain;
import com.example.gimmegonghakauth.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional
public class RecommendServiceSelectManager {

    private final ApplicationContext applicationContext;
    private final UserRepository userRepository;

    // 학과에 따른 추천 서비스를 설정한다.
    public GonghakRecommendService selectRecommendService(Long studentId) {
        MajorsDomain majorsDomain = userRepository.findByStudentId(studentId).get()
                .getMajorsDomain();
        if (majorsDomain.getMajor().contains(ELEC_INFO.getName())) {
            return applicationContext.getBean("elecInfoMajorGonghakRecommendService",
                    ElecInfoMajorGonghakRecommendService.class);
        }
        return applicationContext.getBean("computerMajorGonghakRecommendService",
                ComputerMajorGonghakRecommendService.class);
    }
}
