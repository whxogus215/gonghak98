package com.example.gimmegonghakauth.file.service;

import com.example.gimmegonghakauth.file.service.exception.FileErrorMessage;
import com.example.gimmegonghakauth.file.service.exception.FileException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.apache.poi.ss.usermodel.Workbook;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockMultipartFile;

public class FileServiceTest {

    private FileService fileService;

    @BeforeEach
    void beforeEach() {
        fileService = new FileService();
    }

    @Test
    @DisplayName("사용자가 업로드한 파일의 확장자가 엑셀(xlsx, xls)이 아니면 예외가 발생한다.")
    void fileServiceTest() {
        //given
        MockMultipartFile testFile = new MockMultipartFile(
                "테스트",
                "테스트".getBytes()
        );

        //when & then
        Assertions.assertThatThrownBy(() -> fileService.createWorkbook(testFile))
                .isInstanceOf(FileException.class)
                .hasMessage(FileErrorMessage.ONLY_EXCEL_EXTENSION.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"xlsx", "xls"})
    @DisplayName("사용자가 비어있는 파일을 업로드하면 예외가 발생한다.")
    void fileServiceTest2(final String extension) {
        //given
        MockMultipartFile testFile = new MockMultipartFile(
                "테스트." + extension,
                new byte[0]
        );

        //when & then
        Assertions.assertThatThrownBy(() -> fileService.createWorkbook(testFile))
                .isInstanceOf(FileException.class)
                .hasMessage(FileErrorMessage.FILE_CONTENT_EMPTY.getMessage());
    }

    @Test
    @DisplayName("사용자가 올바른 기이수 성적파일을 업로드하면, 예외가 발생하지 않는다.")
    void validateWorkbookTest1() throws IOException {
        //given
        String fileName = "기이수성적조회";
        String filePath = "src/test/resources/file/기이수성적조회.xlsx";
        File file = new File(filePath);
        MockMultipartFile testFile = new MockMultipartFile(fileName, file.getName(), "xlsx", new FileInputStream(file));
        final Workbook workbook = fileService.createWorkbook(testFile);

        //when & then
        Assertions.assertThatCode(() -> fileService.validateWorkbook(workbook))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("사용자가 잘못된 기이수 성적파일을 업로드하면, 예외가 발생한다.")
    void validateWorkbookTest2() throws IOException {
        //given
        String fileName = "수강신청내역조회";
        String filePath = "src/test/resources/file/수강신청내역조회.xlsx";
        File file = new File(filePath);
        MockMultipartFile testFile = new MockMultipartFile(fileName, file.getName(), "xlsx", new FileInputStream(file));
        final Workbook workbook = fileService.createWorkbook(testFile);

        //when & then
        Assertions.assertThatThrownBy(() -> fileService.validateWorkbook(workbook))
                .isInstanceOf(FileException.class)
                .hasMessage(FileErrorMessage.ONLY_COMPLETED_EXCEL_FILE.getMessage());
    }
}
