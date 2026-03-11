package com.xuanthi.talentmatchingbe.controller;

import com.xuanthi.talentmatchingbe.dto.notification.NotificationResponse;
import com.xuanthi.talentmatchingbe.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "NotificationController", description = "Các API liên quan đến thông báo")
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * LẤY DANH SÁCH THÔNG BÁO (Phân trang)
     * Tự động lấy cho người dùng đang đăng nhập
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()") // Bất kỳ ai login đều có thông báo
    @Operation(summary = "Xem danh sách thông báo")
    public ResponseEntity<Page<NotificationResponse>> getMyNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(notificationService.getMyNotifications(page, size));
    }

    /**
     * ĐÁNH DẤU TẤT CẢ LÀ ĐÃ ĐỌC
     * Dùng PATCH vì chỉ cập nhật trạng thái is_read
     */
    @PatchMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Đánh dấu đã đọc tất cả thông báo")
    public ResponseEntity<String> markAllRead() {
        notificationService.markAllRead();
        return ResponseEntity.ok("Đã đánh dấu tất cả thông báo là đã đọc.");
    }
}