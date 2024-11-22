package com.example.gimmegonghakauth.file.service;

import static com.example.gimmegonghakauth.file.service.exception.FileErrorMessage.FILE_CONTENT_EMPTY;
import static com.example.gimmegonghakauth.file.service.exception.FileErrorMessage.ONLY_EXCEL_EXTENSION;

import com.example.gimmegonghakauth.completed.service.exception.FileException;
import java.io.IOException;
import java.util.Objects;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
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
}
