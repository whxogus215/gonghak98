package com.example.gimmegonghakauth.file.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.gimmegonghakauth.file.service.exception.FileErrorMessage;
import com.example.gimmegonghakauth.file.service.exception.FileException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockMultipartFile;

@SuppressWarnings("NonAsciiCharacters")
public class FileServiceTest {

    public static final int TEST_FILE_ROW_SIZE = 31;
    private FileService fileService;

    @BeforeEach
    void beforeEach() {
        fileService = new FileService();
    }

    @Test
    @DisplayName("사용자가 업로드한 파일의 확장자가 엑셀(xlsx, xls)이 아니면 예외가 발생한다.")
    void fileServiceTest() {
        //given
        MockMultipartFile testFile = new MockMultipartFile("테스트", "테스트".getBytes());

        //when & then
        assertThatThrownBy(() -> fileService.createWorkbook(testFile))
                .isInstanceOf(FileException.class)
                .hasMessage(FileErrorMessage.ONLY_EXCEL_EXTENSION.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"xlsx", "xls"})
    @DisplayName("사용자가 비어있는 파일을 업로드하면 예외가 발생한다.")
    void fileServiceTest2(final String extension) {
        //given
        MockMultipartFile testFile = new MockMultipartFile("테스트." + extension, new byte[0]);

        //when & then
        assertThatThrownBy(() -> fileService.createWorkbook(testFile))
                .isInstanceOf(FileException.class)
                .hasMessage(FileErrorMessage.FILE_CONTENT_EMPTY.getMessage());
    }

    @Test
    @DisplayName("사용자가 올바른 기이수 성적파일을 업로드하면, 예외가 발생하지 않는다.")
    void validateWorkbookTest1() throws IOException {
        //given
        final MockMultipartFile testFile = 기이수성적조회_파일_생성("src/test/resources/file/기이수성적조회.xlsx");
        final Workbook workbook = fileService.createWorkbook(testFile);

        //when & then
        assertThatCode(() -> fileService.validateWorkbook(workbook))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("사용자가 잘못된 기이수 성적파일을 업로드하면, 예외가 발생한다.")
    void validateWorkbookTest2() throws IOException {
        //given
        final MockMultipartFile testFile = 기이수성적조회_파일_생성("src/test/resources/file/수강신청내역조회.xlsx");
        final Workbook workbook = fileService.createWorkbook(testFile);

        //when & then
        assertThatThrownBy(() -> fileService.validateWorkbook(workbook))
                .isInstanceOf(FileException.class)
                .hasMessage(FileErrorMessage.ONLY_COMPLETED_EXCEL_FILE.getMessage());
    }

    @Test
    @DisplayName("파일에서 데이터를 가져와서 과목정보를 갖는 DTO를 생성한다.")
    void getUserCoursesFromFileTest() throws IOException {
        //given
        final MockMultipartFile testFile = 기이수성적조회_파일_생성("src/test/resources/file/기이수성적조회.xlsx");
        final Workbook workbook = fileService.createWorkbook(testFile);

        //when
        final List<UserCourseDto> courses = fileService.getUserCoursesFromFile(workbook);

        //then
        assertThat(courses.size()).isEqualTo(TEST_FILE_ROW_SIZE);
        assertThat(courses).allSatisfy(course -> {
            assertThat(course.courseId()).isNotNull();
            assertThat(course.semester()).isNotNull();
            assertThat(course.year()).isNotZero();
        });
    }

    public static MockMultipartFile 기이수성적조회_파일_생성(final String filePath) throws IOException {
        String fileName = "기이수성적조회";
        File file = new File(filePath);
        return new MockMultipartFile(fileName, file.getName(), "xlsx", new FileInputStream(file));
    }
}
