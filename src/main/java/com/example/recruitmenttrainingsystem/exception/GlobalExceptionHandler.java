package com.example.recruitmenttrainingsystem.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.dao.DataIntegrityViolationException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Bắt lỗi CustomException (Lỗi logic do bạn tự ném ra)
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<Map<String, String>> handleCustomException(CustomException ex) {
        Map<String, String> response = new HashMap<>();
        // Trả về đúng message bạn đã viết trong Service
        response.put("message", ex.getMessage());

        // Trả về mã 400 (Bad Request) thay vì 500 (Internal Server Error)
        // Vì đây là lỗi do người dùng nhập liệu/vi phạm logic, không phải lỗi sập server
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // Bắt các lỗi RuntimeException khác nếu có
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("message", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // Bắt lỗi Validate (@Valid)
    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(
            org.springframework.web.bind.MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String errorMessage = error.getDefaultMessage();
            errors.put("message", errorMessage);
        });
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDatabaseException(DataIntegrityViolationException ex) {
        Map<String, String> response = new HashMap<>();

        // Log lỗi ra console để dev xem (nếu cần)
        System.err.println("Database Error: " + ex.getMessage());

        // Trả về thông báo thân thiện cho người dùng
        response.put("message", "Lỗi dữ liệu: Không thể lưu. Vui lòng kiểm tra lại thông tin (có thể do thiếu trường bắt buộc hoặc dữ liệu không hợp lệ).");

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}