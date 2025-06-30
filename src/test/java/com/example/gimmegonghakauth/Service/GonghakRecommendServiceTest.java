package com.example.gimmegonghakauth.Service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.gimmegonghakauth.common.constant.AbeekTypeConst;
import com.example.gimmegonghakauth.common.domain.MajorsDomain;
import com.example.gimmegonghakauth.status.service.AbeekService;
import com.example.gimmegonghakauth.status.service.dto.GonghakStandardDto;
import com.example.gimmegonghakauth.status.service.dto.IncompletedCoursesDto;
import com.example.gimmegonghakauth.status.service.recommend.ComputerMajorGonghakRecommendService;
import com.example.gimmegonghakauth.status.service.recommend.GonghakRecommendService;
import com.example.gimmegonghakauth.status.service.recommend.RecommendServiceSelectManager;
import com.example.gimmegonghakauth.user.domain.UserDomain;
import com.example.gimmegonghakauth.user.infrastructure.UserRepository;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Slf4j
@Transactional
@ActiveProfiles("test")
class GonghakRecommendServiceTest {

    private static final Long TEST_ID = 19011706L;

    private UserDomain testUser;
    private MajorsDomain testMajor;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RecommendServiceSelectManager recommendServiceSelectManager;

    @Autowired
    private AbeekService abeekService;

    @BeforeEach
    void setUp() {
        testUser = userRepository.findByStudentId(TEST_ID)
                                 .orElseThrow(() -> new IllegalArgumentException("테스트용 유저가 존재하지 않습니다."));
        testMajor = testUser.getMajorsDomain();
    }

    @Test
    @DisplayName("유저의 학과(컴퓨터 공학과)에 맞는 추천 서비스 객체 조회 확인")
    void recommendServiceSelectManagerTest() {
        GonghakRecommendService gonghakRecommendService = recommendServiceSelectManager.selectRecommendService(testMajor);
        assertThat(gonghakRecommendService).isInstanceOf(ComputerMajorGonghakRecommendService.class);
    }

    @Test
    @DisplayName("사용자의 추천 과목 조회 테스트")
    void createRecommendCoursesTest() {
        GonghakRecommendService comGonghakRecommendService = recommendServiceSelectManager.selectRecommendService(testMajor);
        GonghakStandardDto standard = abeekService.findLatestStandardByMajor(testMajor)
                                                  .orElseThrow(() -> new IllegalArgumentException("최신 표준을 찾을 수 없습니다."));
        Map<AbeekTypeConst, List<IncompletedCoursesDto>> recommendCoursesByAbeekType = comGonghakRecommendService.createRecommendCourses(testUser, standard)
                                                                                                                 .getRecommendCoursesByAbeekType();

        log.info("recommendCoursesByAbeekType.keySet()= {}", recommendCoursesByAbeekType.keySet());
        //[PROFESSIONAL_NON_MAJOR, BSM, DESIGN, MAJOR, MINIMUM_CERTI]

        recommendCoursesByAbeekType.keySet().forEach(
            abeekTypeConst -> {
                log.info("abeekTypeConst.name() = {}", abeekTypeConst.name());
                recommendCoursesByAbeekType.get(abeekTypeConst);
                for (IncompletedCoursesDto incompletedCoursesDto : recommendCoursesByAbeekType.get(
                    abeekTypeConst)) {
                    log.info("incompletedCoursesDto.getCourseName= {}", incompletedCoursesDto.getCourseName());
                }
            }
        );

        assertThat(recommendCoursesByAbeekType.get(AbeekTypeConst.PROFESSIONAL_NON_MAJOR).size()).isNotEqualTo(0);

        assertThat(recommendCoursesByAbeekType.keySet()).containsOnly(
            AbeekTypeConst.PROFESSIONAL_NON_MAJOR,
            AbeekTypeConst.BSM,
            AbeekTypeConst.DESIGN,
            AbeekTypeConst.MAJOR,
            AbeekTypeConst.MINIMUM_CERTI
        );
    }
}
