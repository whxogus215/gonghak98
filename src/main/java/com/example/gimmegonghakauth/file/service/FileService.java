package com.example.gimmegonghakauth.file.service;

import static com.example.gimmegonghakauth.file.service.exception.FileErrorMessage.FILE_CONTENT_EMPTY;
import static com.example.gimmegonghakauth.file.service.exception.FileErrorMessage.ONLY_COMPLETED_EXCEL_FILE;
import static com.example.gimmegonghakauth.file.service.exception.FileErrorMessage.ONLY_EXCEL_EXTENSION;

import com.example.gimmegonghakauth.file.service.exception.FileException;
import java.io.IOException;
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
}
