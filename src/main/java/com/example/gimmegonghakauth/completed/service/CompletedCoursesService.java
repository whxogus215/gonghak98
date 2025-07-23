package com.example.gimmegonghakauth.completed.service;

import com.example.gimmegonghakauth.completed.infrastructure.CompletedCoursesDao;
import com.example.gimmegonghakauth.common.infrastructure.CoursesDao;
import com.example.gimmegonghakauth.user.infrastructure.UserRepository;
import com.example.gimmegonghakauth.completed.domain.CompletedCoursesDomain;
import com.example.gimmegonghakauth.common.domain.CourseDomain;
import com.example.gimmegonghakauth.user.domain.UserDomain;
import com.example.gimmegonghakauth.completed.service.exception.FileException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class CompletedCoursesService {

    private final CompletedCoursesDao completedCoursesDao;
    private final CoursesDao coursesDao; // CoursesDao 변수 선언
    private final UserRepository userRepository;

    public CompletedCoursesService(CompletedCoursesDao completedCoursesDao, CoursesDao coursesDao,
        UserRepository userRepository) {
        this.completedCoursesDao = completedCoursesDao;
        this.coursesDao = coursesDao; // 생성자를 통한 CoursesDao 초기화
        this.userRepository = userRepository;
    }

    final int FIRST_ROW = 4;

    public void extractExcelFile(MultipartFile file, UserDetails userDetails)
        throws IOException, FileException { //엑셀 데이터 추출
        //업로드 파일 검증
        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        validateExcelFile(file, extension); //업로드 파일 검증

        //DB에 해당 사용자의 기이수 과목 정보 확인
        Long studentId = Long.parseLong(userDetails.getUsername());
        UserDomain user = userRepository.findByStudentId(studentId).get();
        checkUser(user);

        ////엑셀 내용 검증
        Workbook workbook = creatWorkbook(file, extension);
        Sheet worksheet = workbook.getSheetAt(0);
        DataFormatter dataFormatter = new DataFormatter();
        validateExcelContent(worksheet, dataFormatter);

        //데이터 추출
        extractData(worksheet, dataFormatter, user);
    }

    @Transactional(readOnly = true)
    public List<CompletedCoursesDomain> getExcelList(UserDetails userDetails) {
        Long studentId = Long.parseLong(userDetails.getUsername());
        UserDomain userDomain = userRepository.findByStudentId(studentId).get();

        return completedCoursesDao.findByUserDomain(userDomain);
    }

    @Transactional
    public void extractData(Sheet worksheet, DataFormatter dataFormatter, UserDomain userDomain) {

        List<Long> courseIds = new ArrayList<>();
        List<Map<String, Object>> rowDatas = new ArrayList<>();
        String yearKey = "year";
        String semesterKey = "semesterKey";
        String courseIdKey = "courseId";

        for (int i = FIRST_ROW; i < worksheet.getPhysicalNumberOfRows(); i++) { //데이터 추출
            Row row = worksheet.getRow(i);

            String courseIdAsString = dataFormatter.formatCellValue(row.getCell(3));
            Long courseId = courseIdToLong(courseIdAsString); //학수번호

            Map<String, Object> cell = new HashMap<>();
            cell.put(yearKey, Integer.parseInt(dataFormatter.formatCellValue(row.getCell(1))) % 100);
            cell.put(semesterKey, dataFormatter.formatCellValue(row.getCell(2)));
            cell.put(courseIdKey, courseId);
            rowDatas.add(cell);
            courseIds.add(courseId);
        }

        Map<Long, CourseDomain> findCourses = coursesDao.findAllById(courseIds)
                                                        .stream()
                                                        .collect(Collectors.toMap(CourseDomain::getCourseId, c -> c));
        List<CompletedCoursesDomain> completedCoursesList = new ArrayList<>();  // 저장할 엔티티 리스트 생성
        for(Map<String, Object> rowData : rowDatas) {
            Long courseId = (Long) rowData.get(courseIdKey);
            CourseDomain courseDomain = findCourses.get(courseId); // DB가 아닌 메모리에서 조회
            if (courseDomain == null) {
                continue;
            }

            CompletedCoursesDomain data = CompletedCoursesDomain.builder()
                                                                .userDomain(userDomain)
                                                                .courseDomain(courseDomain)
                                                                .year((Integer) rowData.get(yearKey))
                                                                .semester((String) rowData.get(semesterKey))
                                                                .build();
            completedCoursesList.add(data);  // 엔티티를 리스트에 추가
        }
        completedCoursesDao.saveAll(completedCoursesList);  // 한 번에 전체 엔티티 저장
    }

    //업로드 파일 검증
    private void validateExcelFile(MultipartFile file, String extension) throws FileException {
        if (file.isEmpty()) { // 파일이 비어있으면
            throw new FileException("파일이 비어 있습니다.");
        }
        if (!extension.equals("xlsx") && !extension.equals("xls")) { //엑셀파일이 아니면
            throw new FileException("엑셀 파일만 업로드 해주세요.");
        }
    }

    // 엑셀 내용 검증
    private void validateExcelContent(Sheet workSheet, DataFormatter dataFormatter)
        throws FileException {
        if (workSheet == null) {
            throw new FileException("엑셀파일이 비어있습니다.");
        }
        Row row = workSheet.getRow(0);
        if (row == null) { //엑셀파일이 비어있으면
            throw new FileException("엑셀파일이 비어있습니다.");
        }
        String data = dataFormatter.formatCellValue(row.getCell(0));
        if (!data.equals("기이수성적")) { //형식이 올바르지 않으면
            throw new FileException("기이수성적 엑셀파일을 업로드 해주세요.");
        }
    }

    //확장자에 맞춰서 workbook 리턴
    private Workbook creatWorkbook(MultipartFile file, String extension) throws IOException {
        Workbook workbook = null;
        if (extension.equals("xlsx")) {
            workbook = new XSSFWorkbook(file.getInputStream());
        } else if (extension.equals("xls")) {
            workbook = new HSSFWorkbook(file.getInputStream());
        }
        return workbook;
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
