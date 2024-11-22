package com.example.gimmegonghakauth.Service;

import com.example.gimmegonghakauth.completed.infrastructure.CompletedCoursesDao;
import com.example.gimmegonghakauth.common.infrastructure.CoursesDao;
import com.example.gimmegonghakauth.common.infrastructure.MajorsDao;
import com.example.gimmegonghakauth.user.infrastructure.UserRepository;
import com.example.gimmegonghakauth.completed.domain.CompletedCoursesDomain;
import com.example.gimmegonghakauth.common.domain.CoursesDomain;
import com.example.gimmegonghakauth.user.domain.UserDomain;
import com.example.gimmegonghakauth.completed.service.CompletedCoursesService;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest
@Transactional
@Nested
@DisplayName("DB 테스트(기이수과목)")
@Import(CompletedCoursesService.class)
@ActiveProfiles("test")
public class CompletedCoursesServiceDataTest {

    @Autowired
    private MajorsDao majorsDao;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompletedCoursesDao completedCoursesDao;

    @Autowired
    private CoursesDao coursesDao;

    @Autowired
    private CompletedCoursesService completedCoursesService;

    @BeforeEach
    public void setCourses() {
        //과목 Entity save
        CoursesDomain coursesDomain1 = CoursesDomain.builder()
            .courseId(12345L).name("test1").credit(3)
            .build();
        CoursesDomain coursesDomain2 = CoursesDomain.builder()
            .courseId(54321L).name("test2").credit(3)
            .build();

        coursesDao.save(coursesDomain1);
        coursesDao.save(coursesDomain2);
    }

    @BeforeEach
    public void setUser() {
        //유저 Entity save
        UserDomain user = UserDomain.builder().studentId(19111111L)
            .password("qwer").email("test@gmail.com")
            .majorsDomain(majorsDao.findByMajor("컴퓨터공학과"))
            .name("testUser")
            .build();
        userRepository.save(user);
    }

    @Test
    @DisplayName("업로드 데이터 저장 테스트")
    /**
     * CompletedCoursesService(C.C.S)에 대한 테스트인지가 의문이 들었음.
     * - C.C.S와 의존관계인 Dao 객체의 메서드만 호출하지 정작 서비스 계층의 메서드는 호출하지 않고 있음.
     * - 서비스 계층에 대한 테스트 보다는 Dao(리포지토리)에 대한 테스트에 가까워 보임.
     */
    public void testUploadFile() {
        //기이수 과목 데이터1
        CompletedCoursesDomain data1 =
            CompletedCoursesDomain.builder().
                userDomain(userRepository.findByStudentId(19111111L).get()).
                coursesDomain(coursesDao.findByCourseId(12345L)).
                year(23).semester("1학기").
                build();

        //기이수 과목 데이터2
        CompletedCoursesDomain data2 =
            CompletedCoursesDomain.builder().
                userDomain(userRepository.findByStudentId(19111111L).get()).
                coursesDomain(coursesDao.findByCourseId(54321L)).
                year(23).semester("1학기").
                build();

        //기이수 과목 저장
        completedCoursesDao.save(data1);
        completedCoursesDao.save(data2);

        List<CompletedCoursesDomain> dataList = new ArrayList<>();

        dataList.add(data1);
        dataList.add(data2);

        UserDomain user = userRepository.findByStudentId(19111111L).get();
        assertEquals(dataList, completedCoursesDao.findByUserDomain(user));
    }

    @Test
    @DisplayName("재업로드 테스트1(첫 업로드)")
    // 서비스의 checkUser라는 메서드를 테스트하는 메서드인 것 같음.
    public void testUserUploadStatus1() {
        UserDomain user = userRepository.findByStudentId(19111111L).get();

        //데이터 확인
        completedCoursesService.checkUser(user);

        //해당 유저 검색
        List<CompletedCoursesDomain> deletedDataList = completedCoursesDao.findByUserDomain(user);

        //검색한 결과가 비어있는지 확인
        assertTrue(deletedDataList.isEmpty());
    }

    @Test
    @DisplayName("재업로드 테스트2(재업로드,단일)")
    // 서비스의 checkUser라는 메서드를 테스트하는 메서드인 것 같음. 222
    public void testUserUploadStatus2() {
        //기이수 과목 데이터 1
        CompletedCoursesDomain data1 =
            CompletedCoursesDomain.builder().
                userDomain(userRepository.findByStudentId(19111111L).get()).
                coursesDomain(coursesDao.findByCourseId(12345L)).
                year(23).semester("1학기").
                build();

        completedCoursesDao.save(data1);

        List<CompletedCoursesDomain> dataList = new ArrayList<>();
        dataList.add(data1);

        UserDomain user = userRepository.findByStudentId(19111111L).get();

        //데이터 삭제
        completedCoursesService.checkUser(user);

        //해당 유저 검색
        List<CompletedCoursesDomain> deletedDataList = completedCoursesDao.findByUserDomain(user);
        System.out.println("Deleted Data List: " + deletedDataList);
        //검색한 결과가 비어있는지 확인
        assertTrue(deletedDataList.isEmpty());
    }

    @Test
    @DisplayName("재업로드 테스트3(재업로드,복수)")
    public void testUserUploadStatus3() {
        //기이수 과목 데이터 1
        CompletedCoursesDomain data1 =
            CompletedCoursesDomain.builder().
                userDomain(userRepository.findByStudentId(19111111L).get()).
                coursesDomain(coursesDao.findByCourseId(12345L)).
                year(23).semester("1학기").
                build();
        //기이수 과목 데이터 2
        CompletedCoursesDomain data2 =
            CompletedCoursesDomain.builder().
                userDomain(userRepository.findByStudentId(19111111L).get()).
                coursesDomain(coursesDao.findByCourseId(12345L)).
                year(23).semester("1학기").
                build();

        completedCoursesDao.save(data1);
        completedCoursesDao.save(data2);

        List<CompletedCoursesDomain> dataList = new ArrayList<>();
        dataList.add(data1);
        dataList.add(data2);

        UserDomain user = userRepository.findByStudentId(19111111L).get();

        //데이터 삭제
        // checkUser를 호출했을 때 왜 데이터가 삭제되는거지?
        completedCoursesService.checkUser(user);

        //해당 유저 검색
        List<CompletedCoursesDomain> deletedDataList = completedCoursesDao.findByUserDomain(user);
        System.out.println("Deleted Data List: " + deletedDataList);
        //검색한 결과가 비어있는지 확인
        assertTrue(deletedDataList.isEmpty());
    }


}
