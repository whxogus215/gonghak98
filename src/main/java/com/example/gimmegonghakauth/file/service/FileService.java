package com.example.gimmegonghakauth.file.service;

import static com.example.gimmegonghakauth.file.service.exception.FileErrorMessage.FILE_CONTENT_EMPTY;
import static com.example.gimmegonghakauth.file.service.exception.FileErrorMessage.ONLY_COMPLETED_EXCEL_FILE;
import static com.example.gimmegonghakauth.file.service.exception.FileErrorMessage.ONLY_EXCEL_EXTENSION;

import com.example.gimmegonghakauth.file.service.exception.FileException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileService {

    private final int FIRST_ROW = 4;

    public Workbook createWorkbook(final MultipartFile file) throws IOException {
        final String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        validateFile(file, extension);
        if (extension.equals("xlsx")) {
            return new XSSFWorkbook(file.getInputStream());
        } else {
            return new HSSFWorkbook(file.getInputStream());
        }
    }

    private void validateFile(final MultipartFile file, final String extension) {
        if (file.isEmpty()) {
            throw new FileException(FILE_CONTENT_EMPTY.getMessage());
        }
        if (!Objects.equals(extension, "xlsx") && !Objects.equals(extension, "xls")) {
            throw new FileException(ONLY_EXCEL_EXTENSION.getMessage());
        }
    }

    public void validateWorkbook(final Workbook workbook) {
        Sheet worksheet = workbook.getSheetAt(0); // 업로드한 엑셀파일의 첫 번째 시트를 가져옵니다.
        DataFormatter dataFormatter = new DataFormatter();

        if (worksheet == null) {
            throw new FileException(FILE_CONTENT_EMPTY.getMessage());
        }
        Row row = worksheet.getRow(0);
        if (row == null) { //엑셀파일이 비어있으면
            throw new FileException(FILE_CONTENT_EMPTY.getMessage());
        }
        String headerMessage = dataFormatter.formatCellValue(row.getCell(0));
        if (!headerMessage.equals("기이수성적")) { //형식이 올바르지 않으면
            throw new FileException(ONLY_COMPLETED_EXCEL_FILE.getMessage());
        }
    }

    public List<UserCourseDto> getUserCoursesFromFile(final Workbook workbook) {
        Sheet worksheet = workbook.getSheetAt(0);
        DataFormatter dataFormatter = new DataFormatter();
        List<UserCourseDto> result = new ArrayList<>();
        for (int i = FIRST_ROW; i < worksheet.getPhysicalNumberOfRows(); i++) {
            Row row = worksheet.getRow(i);

            //년도
            String yearAsString = dataFormatter.formatCellValue(row.getCell(1));
            int year = Integer.parseInt(yearAsString) % 100;
            //학기
            String semester = dataFormatter.formatCellValue(row.getCell(2));
            //학수번호
            String courseIdAsString = dataFormatter.formatCellValue(row.getCell(3));
            Long courseId = parseCourseIdToLong(courseIdAsString);

            result.add(new UserCourseDto(courseId, year, semester));
        }
        return result;
    }

    private Long parseCourseIdToLong(String courseIdAsString) {
        if (!Character.isDigit(courseIdAsString.charAt(0))){
            if (courseIdAsString.charAt(0) == 'P'){
                courseIdAsString = '0' + courseIdAsString.substring(1);
            } else {
                // 학수번호가 문자로 시작하는 과목은 저장하지 않습니다.(현장실습(P)과목 제외)
                return 0L;
            }
        }
        return Long.parseLong(courseIdAsString);
    }
}
