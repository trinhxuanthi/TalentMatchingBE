package com.xuanthi.talentmatchingbe.controller;

import com.xuanthi.talentmatchingbe.dto.notification.NotificationResponse;
import com.xuanthi.talentmatchingbe.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notification Controller", description = "Các API liên quan đến thông báo")
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
        try {
            // Validate pagination parameters
            if (page < 0) {
                log.warn("Invalid page number: {}", page);
                return ResponseEntity.badRequest().build();
            }
            if (size <= 0 || size > 100) {
                log.warn("Invalid page size: {}", size);
                return ResponseEntity.badRequest().build();
            }

            log.debug("Fetching notifications - page: {}, size: {}", page, size);
            return ResponseEntity.ok(notificationService.getMyNotifications(page, size));
        } catch (Exception e) {
            log.error("Error fetching notifications: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * ĐÁNH DẤU TẤT CẢ LÀ ĐÃ ĐỌC
     * Dùng PATCH vì chỉ cập nhật trạng thái is_read
     */
    @PatchMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Đánh dấu đã đọc tất cả thông báo")
    public ResponseEntity<String> markAllRead() {
        try {
            log.debug("Marking all notifications as read");
            notificationService.markAllRead();
            return ResponseEntity.ok("Đã đánh dấu tất cả thông báo là đã đọc.");
        } catch (RuntimeException e) {
            log.warn("Error marking notifications as read: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            log.error("Unexpected error marking notifications as read: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * LẤY SỐ LƯỢNG CHƯA ĐỌC (Cho cái chấm đỏ trên icon chuông)
     */
    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lấy số lượng thông báo chưa đọc")
    public ResponseEntity<?> getUnreadCount() {
        try {
            long count = notificationService.getUnreadCount();
            // Trả về JSON { "unreadCount": 5 } để Frontend dễ bóc tách
            return ResponseEntity.ok(java.util.Map.of("unreadCount", count));
        } catch (Exception e) {
            log.error("Error fetching unread count: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * ĐÁNH DẤU 1 THÔNG BÁO LÀ ĐÃ ĐỌC (Khi ứng viên click vào 1 dòng)
     */
    @PatchMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Đánh dấu 1 thông báo cụ thể là đã đọc")
    public ResponseEntity<?> markOneAsRead(@PathVariable Long id) {
        try {
            notificationService.markOneAsRead(id);
            return ResponseEntity.ok(java.util.Map.of("message", "Đã đọc thông báo."));
        } catch (Exception e) {
            log.error("Error marking notification {} as read: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * XÓA MỀM THÔNG BÁO (Khi ứng viên bấm dấu X để dọn dẹp)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Xóa (ẩn) 1 thông báo cụ thể")
    public ResponseEntity<?> deleteNotification(@PathVariable Long id) {
        try {
            notificationService.deleteNotification(id);
            return ResponseEntity.ok(java.util.Map.of("message", "Đã xóa thông báo."));
        } catch (Exception e) {
            log.error("Error deleting notification {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}