package com.example.demo.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(TokenExpiredException.class)
    ResponseEntity<ApiResponse> handleTokenExpired(TokenExpiredException ex) {
        log.error("TokenExpiredException: ", ex);
        ApiResponse apiResponse = ApiResponse.builder()
                .code(1001) // code riêng cho token expired
                .message(ex.getMessage())
                .build();
        return ResponseEntity.badRequest().body(apiResponse);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiResponse> handleGeneralException(Exception ex) {
        log.error("Exception: ", ex);
        ApiResponse apiResponse = ApiResponse.builder()
                .code(1000)
                .message("Internal server error")
                .build();
        return ResponseEntity.internalServerError().body(apiResponse);
    }
}
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
class ApiResponse<T> {
    @Builder.Default
    private int code = 1000;

    private String message;
    private T result;
}
