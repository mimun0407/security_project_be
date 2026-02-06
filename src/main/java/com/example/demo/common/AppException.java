package com.example.demo.common;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AppException extends RuntimeException {
    private HttpStatus status;
    private String errorCode; // Ví dụ: USER_NOT_FOUND, EMAIL_EXISTS

    public AppException(HttpStatus status, String errorCode, String message) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

    // Constructor nhanh cho lỗi 400 Bad Request
    public AppException(String errorCode, String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
        this.errorCode = errorCode;
    }
}
