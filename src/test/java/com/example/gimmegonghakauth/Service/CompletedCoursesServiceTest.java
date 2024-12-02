package com.example.gimmegonghakauth.Service;

import static com.example.gimmegonghakauth.file.service.FileServiceTest.TEST_FILE_ROW_SIZE;
import static com.example.gimmegonghakauth.file.service.FileServiceTest.기이수성적조회_파일_생성;
import static org.assertj.core.api.Assertions.assertThat;

import com.example.gimmegonghakauth.completed.domain.CompletedCoursesDomain;
import com.example.gimmegonghakauth.completed.service.CompletedCoursesService;
import com.example.gimmegonghakauth.user.domain.UserDomain;
import com.example.gimmegonghakauth.user.infrastructure.UserRepository;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;


@SpringBootTest
@Transactional
@Import(CompletedCoursesService.class)
@ActiveProfiles("test")
@TestInstance(Lifecycle.PER_CLASS)
@SuppressWarnings("NonAsciiCharacters")
public class CompletedCoursesServiceTest {

    private static final long TEST_ID = 19111111L;
    private UserDomain user;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompletedCoursesService completedCoursesService;

    @BeforeAll
    void setUser() {
        //유저 Entity save
        user = UserDomain.builder().studentId(TEST_ID)
                .password("qwer").email("test@gmail.com")
                .majorsDomain(null)
                .name("testUser")
                .build();
        userRepository.save(user);
    }

    @Test
    @DisplayName("기이수 성적 파일을 업로드하면, 기이수 성적 과목이 저장된다.")
    /**
     * CompletedCoursesService(C.C.S)에 대한 테스트인지가 의문이 들었음.
     * - C.C.S와 의존관계인 Dao 객체의 메서드만 호출하지 정작 서비스 계층의 메서드는 호출하지 않고 있음.
     * - 서비스 계층에 대한 테스트 보다는 Dao(리포지토리)에 대한 테스트에 가까워 보임.
     */
    public void saveCompletedCoursesTest() throws IOException {
        //given
        MockMultipartFile testFile = 기이수성적조회_파일_생성("src/test/resources/file/기이수성적조회.xlsx");

        //when
        completedCoursesService.saveCompletedCourses(testFile, TEST_ID);

        //then
        List<CompletedCoursesDomain> findCourses = completedCoursesService.getCompletedCourses(TEST_ID);
        assertThat(findCourses.size()).isEqualTo(TEST_FILE_ROW_SIZE);
    }

    @Test
    @DisplayName("성적파일을 업로드했던 유저가 다시 업로드하면, 초기화 후 새로 저장된다.")
    public void saveCompletedCoursesTest2() throws IOException {
        //given
        MockMultipartFile testFile = 기이수성적조회_파일_생성("src/test/resources/file/기이수성적조회.xlsx");
        MockMultipartFile secondTestFile = 기이수성적조회_파일_생성("src/test/resources/file/기이수성적조회_46과목.xlsx");
        int secondRowSize = 46;
        completedCoursesService.saveCompletedCourses(testFile, TEST_ID);

        //when
        completedCoursesService.saveCompletedCourses(secondTestFile, TEST_ID);

        //then
        List<CompletedCoursesDomain> findCourses = completedCoursesService.getCompletedCourses(TEST_ID);
        assertThat(findCourses.size()).isEqualTo(secondRowSize);
        assertThat(findCourses.size()).isNotEqualTo(TEST_FILE_ROW_SIZE);
    }
}
