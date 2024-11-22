package com.example.gimmegonghakauth.file.service.exception;

public enum FileErrorMessage {

    ONLY_EXCEL_EXTENSION("엑셀 파일만 업로드 해주세요."),
    FILE_CONTENT_EMPTY("파일이 비어 있습니다.");

    private String message;

    FileErrorMessage(final String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
