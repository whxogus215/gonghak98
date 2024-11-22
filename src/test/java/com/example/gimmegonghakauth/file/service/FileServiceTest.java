package com.example.gimmegonghakauth.file.service;

import com.example.gimmegonghakauth.completed.service.exception.FileException;
import com.example.gimmegonghakauth.file.service.exception.FileErrorMessage;
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
        Assertions.assertThatThrownBy(() -> fileService.validateFile(testFile))
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
        Assertions.assertThatThrownBy(() -> fileService.validateFile(testFile))
                .isInstanceOf(FileException.class)
                .hasMessage(FileErrorMessage.FILE_CONTENT_EMPTY.getMessage());
    }
}
