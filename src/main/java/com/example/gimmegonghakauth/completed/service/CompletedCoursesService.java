package com.example.gimmegonghakauth.completed.service;

import com.example.gimmegonghakauth.common.domain.CoursesDomain;
import com.example.gimmegonghakauth.common.infrastructure.CoursesDao;
import com.example.gimmegonghakauth.completed.domain.CompletedCoursesDomain;
import com.example.gimmegonghakauth.completed.infrastructure.CompletedCoursesDao;
import com.example.gimmegonghakauth.file.service.FileService;
import com.example.gimmegonghakauth.file.service.UserCourseDto;
import com.example.gimmegonghakauth.file.service.exception.FileException;
import com.example.gimmegonghakauth.user.domain.UserDomain;
import com.example.gimmegonghakauth.user.service.UserService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
@RequiredArgsConstructor
public class CompletedCoursesService {

    private final CompletedCoursesDao completedCoursesDao;
    private final CoursesDao coursesDao; // CoursesDao 변수 선언
    private final UserService userService;
    private final FileService fileService;

    public void saveCompletedCourses(MultipartFile file, Long studentId) throws IOException, FileException {
        //엑셀 데이터 추출
        //업로드 파일 검증
        Workbook workbook = fileService.createWorkbook(file);

        //엑셀 내용 검증
        /**
         * 이것도 File의 책임인 것 같다.
         * Excel Sheet를 가져와서 해당 내용을 검증하는 기능
         */
        fileService.validateWorkbook(workbook);
        List<UserCourseDto> userCourseDtos = fileService.getUserCoursesFromFile(workbook);

        //DB에 해당 사용자의 기이수 과목 정보 확인
        UserDomain user = userService.getByStudentId(studentId);

        saveCompletedCourses(userCourseDtos, user);
    }

    @Transactional
    public void saveCompletedCourses(List<UserCourseDto> userCourseDtos, UserDomain userDomain) {
        // CompletedCourses 테이블에서 파일을 업로드한 유저정보를 가지는 행들을 불러옴
        List<CompletedCoursesDomain> findCourses = completedCoursesDao.findByUserDomain(userDomain);
        // List가 Empty 가 아니면 (해당 유저가 파일을 업로드한 적이 있으면)
        if (!findCourses.isEmpty()) {
            // CompletedCourses 테이블에서 해당하는 행들을 삭제
            completedCoursesDao.deleteAllInBatch(findCourses);
        }

        List<CompletedCoursesDomain> saveCourses = new ArrayList<>();  // 저장할 엔티티 리스트 생성
        for (UserCourseDto userCourse : userCourseDtos) {
            // 학수번호를 기반으로 Courses 테이블 검색
            CoursesDomain coursesDomain = coursesDao.findByCourseId(userCourse.courseId());
            if (coursesDomain == null) {
                continue;
            }
            CompletedCoursesDomain data = CompletedCoursesDomain.builder()
                    .userDomain(userDomain)
                    .coursesDomain(coursesDomain)
                    .year(userCourse.year())
                    .semester(userCourse.semester())
                    .build();
            saveCourses.add(data);  // 엔티티를 리스트에 추가
        }
        completedCoursesDao.saveAll(saveCourses);  // 한 번에 전체 엔티티 저장
    }

    @Transactional(readOnly = true)
    public List<CompletedCoursesDomain> getCompletedCourses(Long studentId) {
        UserDomain userDomain = userService.getByStudentId(studentId);
        return completedCoursesDao.findByUserDomain(userDomain);
    }
}
