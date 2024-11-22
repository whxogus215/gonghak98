package com.example.gimmegonghakauth.file.service;

import static com.example.gimmegonghakauth.file.service.exception.FileErrorMessage.FILE_CONTENT_EMPTY;
import static com.example.gimmegonghakauth.file.service.exception.FileErrorMessage.ONLY_EXCEL_EXTENSION;

import com.example.gimmegonghakauth.completed.service.exception.FileException;
import java.util.Objects;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileService {

    public void validateFile(MultipartFile file) {
        final String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        if (file.isEmpty()) {
            throw new FileException(FILE_CONTENT_EMPTY.getMessage());
        }
        if (!Objects.equals(extension, "xlsx") && !Objects.equals(extension, "xls")) {
            throw new FileException(ONLY_EXCEL_EXTENSION.getMessage());
        }
    }
}
