package com.example.gimmegonghakauth.completed.service;

import com.example.gimmegonghakauth.common.domain.CoursesDomain;
import com.example.gimmegonghakauth.common.infrastructure.CoursesDao;
import com.example.gimmegonghakauth.completed.domain.CompletedCoursesDomain;
import com.example.gimmegonghakauth.completed.infrastructure.CompletedCoursesDao;
import com.example.gimmegonghakauth.file.service.exception.FileException;
import com.example.gimmegonghakauth.file.service.FileService;
import com.example.gimmegonghakauth.user.domain.UserDomain;
import com.example.gimmegonghakauth.user.infrastructure.UserRepository;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
@RequiredArgsConstructor
public class CompletedCoursesService {

    /**
     * 1. CompletedCoursesService가 기이수 과목을 처리하는 느낌보다는 엑셀 파일을 다루는 책임이 더 많게 느껴졌다.
     * 2. 따라서 관련 메서드들이 기이수 과목을 어떻게 한다는건지 한 번에 이해하기가 어려웠다.
     */

    private final CompletedCoursesDao completedCoursesDao;
    private final CoursesDao coursesDao; // CoursesDao 변수 선언
    private final UserRepository userRepository;
    private final FileService fileService;

    final int FIRST_ROW = 4;

    /**
     * 해당 메서드는 결국 엑셀파일에서 추출한 데이터를 기이수 과목으로 만들어서 저장하는 것 같다.
     * 기이수 서비스라면, 기이수 과목을 저장하는 책임만 가지면 되는거 아닐까?
     * 현재 메서드에는
     * 1. 사용자가 업로드한 파일을 검증한다.
     * 2. 사용자가 이전에 파일을 업로드한 적이 있으면, 데이터를 싹 지운다.
     * 3. 엑셀 파일에서 추출한 데이터를 검증한다.
     * 4. 기이수 과목 데이터를 저장한다.
     *
     * 여기서 기이수서비스의 핵심적인 책임은 무엇일까? 2번과 4번 같다.
     * 1번과 3번은 업로드한 파일에 관련된 내용이며, 외부 의존성이라고 생각한다.
     * 그리고 2번과 3번의 순서를 바꿔도 될 것 같다.
     * 사용자가 업로드한 파일의 검증을 실패하면 애초에 저장을 하면 안되기 때문이다.
     *
     * 그러면, 기이수 서비스에 외부 파일과 관련된 의존성을 제거할 수 있다.
     * 외부 데이터와 관련된 의존성은 별도의 파일 서비스로 분리하면 SRP 원칙을 지키며, 단위 테스트도 가능할 것 같다.
     */
    public void extractExcelFile(MultipartFile file, UserDetails userDetails)
        throws IOException, FileException { //엑셀 데이터 추출
        //업로드 파일 검증
        Workbook workbook = fileService.createWorkbook(file);

        //엑셀 내용 검증
        /**
         * 이것도 File의 책임인 것 같다.
         * Excel Sheet를 가져와서 해당 내용을 검증하는 기능
         */
        fileService.validateWorkbook(workbook);

        //DB에 해당 사용자의 기이수 과목 정보 확인
        Long studentId = Long.parseLong(userDetails.getUsername());
        UserDomain user = userRepository.findByStudentId(studentId).get();
        checkUser(user);

        //데이터 추출
//        extractData(worksheet, dataFormatter, user);
    }

    @Transactional(readOnly = true)
    public List<CompletedCoursesDomain> getExcelList(UserDetails userDetails) {
        Long studentId = Long.parseLong(userDetails.getUsername());
        UserDomain userDomain = userRepository.findByStudentId(studentId).get();

        return completedCoursesDao.findByUserDomain(userDomain);
    }

    @Transactional
    public void extractData(Sheet worksheet, DataFormatter dataFormatter, UserDomain userDomain) {
        List<CompletedCoursesDomain> completedCoursesList = new ArrayList<>();  // 저장할 엔티티 리스트 생성

        for (int i = FIRST_ROW; i < worksheet.getPhysicalNumberOfRows(); i++) { //데이터 추출
            Row row = worksheet.getRow(i);

            String yearAsString = dataFormatter.formatCellValue(row.getCell(1));
            int year = Integer.parseInt(yearAsString) % 100;  //년도

            String semester = dataFormatter.formatCellValue(row.getCell(2)); //학기

            String courseIdAsString = dataFormatter.formatCellValue(row.getCell(3));
            Long courseId = courseIdToLong(courseIdAsString); //학수번호

            CoursesDomain coursesDomain = coursesDao.findByCourseId(courseId);// 학수번호를 기반으로 Courses 테이블 검색
            if (coursesDomain == null) {
                continue;
            }
            CompletedCoursesDomain data = CompletedCoursesDomain.builder()
                                                                .userDomain(userDomain)
                                                                .coursesDomain(coursesDomain)
                                                                .year(year)
                                                                .semester(semester)
                                                                .build();
            completedCoursesList.add(data);  // 엔티티를 리스트에 추가
        }
        completedCoursesDao.saveAll(completedCoursesList);  // 한 번에 전체 엔티티 저장
    }

    @Transactional
    public void checkUser(UserDomain userDomain) {
        // CompletedCourses 테이블에서 파일을 업로드한 유저정보를 가지는 행들을 불러옴
        List<CompletedCoursesDomain> coursesList = completedCoursesDao.findByUserDomain(userDomain);
        // List가 Empty 가 아니면 (해당 유저가 파일을 업로드한 적이 있으면)
        if (!coursesList.isEmpty()) {
            // CompletedCourses 테이블에서 해당하는 행들을 삭제
            completedCoursesDao.deleteAllInBatch(coursesList);
        }
    }

    private Long courseIdToLong(String courseIdAsString) {
        if (!Character.isDigit(courseIdAsString.charAt(0))){
            if (courseIdAsString.charAt(0) == 'P'){
                courseIdAsString = '0' + courseIdAsString.substring(1);
            } else{
                return 0L;
            }
        }
        return Long.parseLong(courseIdAsString);
    }
}
