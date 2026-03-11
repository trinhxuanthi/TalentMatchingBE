package com.xuanthi.talentmatchingbe.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice // Đánh dấu đây là nơi xử lý lỗi cho toàn bộ Controller
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntimeException(RuntimeException e) {
        // TỐI ƯU: Trả về mã 400 (Bad Request) thay vì 200 hay 500
        // Frontend sẽ dựa vào status 400 để hiện thông báo lỗi
        return ResponseEntity.badRequest().body(Map.of(
                "status", 400,
                "message", e.getMessage() // Chính là câu "Tài khoản đã bị khóa..." bạn viết ở Service
        ));
    }
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                "status", 403,
                "message", "Bạn không có quyền thực hiện hành động này!"
        ));
    }
}