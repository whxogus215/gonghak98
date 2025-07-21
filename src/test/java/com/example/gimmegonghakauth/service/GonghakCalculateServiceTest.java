package com.example.gimmegonghakauth.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.gimmegonghakauth.service.GonghakCalculateServiceTest.CalculateTestConfig;
import com.example.gimmegonghakauth.common.constant.AbeekTypeConst;
import com.example.gimmegonghakauth.common.domain.MajorsDomain;
import com.example.gimmegonghakauth.status.infrastructure.AbeekDao;
import com.example.gimmegonghakauth.status.infrastructure.GonghakCoursesDao;
import com.example.gimmegonghakauth.status.infrastructure.GonghakDao;
import com.example.gimmegonghakauth.status.infrastructure.GonghakRepository;
import com.example.gimmegonghakauth.status.service.GonghakCalculateService;
import com.example.gimmegonghakauth.status.service.dto.AbeekDetailsDto;
import com.example.gimmegonghakauth.status.service.dto.CourseDetailsDto;
import com.example.gimmegonghakauth.user.domain.UserDomain;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Import(CalculateTestConfig.class)
@Slf4j
@ActiveProfiles("test")
class GonghakCalculateServiceTest {

    private static final MajorsDomain TEST_MAJORSDOMAIN = MajorsDomain.builder()
        .id(1L)
        .major("컴퓨터공학과").build();
    private static final UserDomain TEST_USERDOMAIN = UserDomain.builder()
        .email("testEmail")
        .name("홍지섭")
        .password("qwer")
        .studentId(19011706L)
        .majorsDomain(TEST_MAJORSDOMAIN).build();

    @Autowired
    private GonghakCalculateService gonghakCalculateService;

    @Test
    @DisplayName("check log gonghakCalculateServiceTest")
    void logGonghakCalculateServiceTest() {
        Map<AbeekTypeConst, AbeekDetailsDto> userResultRatio = gonghakCalculateService.getResult(
            TEST_USERDOMAIN).get().getUserResult();

        log.info("userResultRatio = {}", userResultRatio);
    }

    @Test
    @DisplayName("컴퓨터공학과 GonghakCalculateService 계산 결과 체크")
    void correctGonghakCalculateServiceTestCom() {
        Map<AbeekTypeConst, AbeekDetailsDto> userResultRatio = gonghakCalculateService.getResult(
            TEST_USERDOMAIN).get().getUserResult();

        userResultRatio.keySet().forEach(
            abeekTypeConst -> {
                Double userPoint = userResultRatio.get(abeekTypeConst).getResultPoint().getUserPoint();
                assertThat(userPoint).isLessThanOrEqualTo(1);
                assertThat(userPoint).isGreaterThanOrEqualTo(0);
            }
        );
    }

    @Test
    @DisplayName("AbeekTypeConst 별로 CourseDetailsDto가 올바르게 저장되는지 확인")
    void checkCoursesStoredByAbeekTypeConst() {
        Map<AbeekTypeConst, AbeekDetailsDto> userResult = gonghakCalculateService.getResult(TEST_USERDOMAIN).get().getUserResult();

        userResult.forEach((abeekTypeConst, abeekDetailsDto) -> {
            List<CourseDetailsDto> courses = abeekDetailsDto.getCoursesDetails();
            assertThat(courses).isNotNull();
            assertThat(courses).isInstanceOf(List.class);

            log.info("AbeekTypeConst: {}, Courses: {}", abeekTypeConst, courses);
        });
    }


    private double getExpectedCredit(int credit, Map<AbeekTypeConst, Integer> standard,
        AbeekTypeConst abeekTypeConst) {
        return Double.valueOf(
            String.format("%.4f", (double) credit / standard.get(abeekTypeConst)));
    }

    @TestConfiguration
    @RequiredArgsConstructor
    static class CalculateTestConfig {

        private final AbeekDao abeekDao;
        private final GonghakCoursesDao gonghakCoursesDao;

        @Bean
        public GonghakRepository gonghakRepository() {
            return new GonghakDao(abeekDao, gonghakCoursesDao);
        }

        @Bean
        public GonghakCalculateService gonghakCalculateService() {
            return new GonghakCalculateService(gonghakRepository());
        }
    }
}
