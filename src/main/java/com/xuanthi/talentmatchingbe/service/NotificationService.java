package com.xuanthi.talentmatchingbe.service;

import com.xuanthi.talentmatchingbe.dto.notification.NotificationResponse;
import com.xuanthi.talentmatchingbe.entity.Notification;
import com.xuanthi.talentmatchingbe.entity.User;
import com.xuanthi.talentmatchingbe.repository.NotificationRepository;
import com.xuanthi.talentmatchingbe.repository.UserRepository;
import com.xuanthi.talentmatchingbe.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate; // 🚀 Import thêm
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate; // 🚀 Khẩu súng bắn Real-time

    /**
     * Gửi thông báo cho người dùng (Dùng nội bộ hệ thống)
     */
    @Transactional
    public void sendNotification(Long receiverId, Long senderId, String title, String message, String type, Long relatedId) {
        // Validate inputs (Giữ nguyên code cực cẩn thận của sếp)
        if (receiverId == null) {
            log.error("Receiver ID cannot be null");
            throw new RuntimeException("ID người nhận không được null!");
        }
        if (!StringUtils.hasText(title)) {
            throw new RuntimeException("Tiêu đề thông báo không được để trống!");
        }
        if (!StringUtils.hasText(message)) {
            throw new RuntimeException("Nội dung thông báo không được để trống!");
        }

        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Người nhận không tồn tại"));

        String senderAvatar = null;
        if (senderId != null) {
            senderAvatar = userRepository.findById(senderId).map(User::getAvatar).orElse(null);
        }

        Notification noti = Notification.builder()
                .user(receiver)
                .senderId(senderId)
                .senderAvatar(senderAvatar)
                .title(title)
                .message(message)
                .type(type)
                .relatedId(relatedId)
                .build();

        // 1. Lưu vào Database
        Notification savedNoti = notificationRepository.save(noti);
        log.info("Notification saved successfully to DB for user: {}", receiverId);

        // 2. 🚀 BẮN REAL-TIME XUỐNG FRONTEND (Bọc try-catch để lỗi mạng không làm rollback DB)
        try {
            NotificationResponse response = mapToResponse(savedNoti);
            messagingTemplate.convertAndSendToUser(
                    receiver.getEmail(),
                    "/queue/notifications",
                    response
            );
            log.info("Real-time WebSocket sent to: {}", receiver.getEmail());
        } catch (Exception e) {
            log.error("Lỗi khi bắn WebSocket tới user {}: {}", receiver.getEmail(), e.getMessage());
        }
    }

    /**
     * Lấy danh sách thông báo của người dùng hiện tại (phân trang)
     */
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getMyNotifications(int page, int size) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) throw new RuntimeException("Vui lòng đăng nhập để xem thông báo!");

        return notificationRepository.findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(
                        currentUser.getId(), PageRequest.of(page, size))
                .map(this::mapToResponse);
    }

    // ==========================================
    // 🚀 BỔ SUNG CÁC HÀM MỚI THEO REPOSITORY
    // ==========================================

    /**
     * Đếm số lượng thông báo chưa đọc (Dành cho icon chuông)
     */
    @Transactional(readOnly = true)
    public long getUnreadCount() {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) return 0;
        return notificationRepository.countByUserIdAndIsReadFalseAndIsDeletedFalse(currentUser.getId());
    }

    /**
     * Đánh dấu TẤT CẢ là đã đọc
     */
    @Transactional
    public void markAllRead() {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) throw new RuntimeException("Vui lòng đăng nhập!");

        notificationRepository.markAllAsRead(currentUser.getId());
        log.info("Đã đánh dấu toàn bộ thông báo là đã đọc cho user: {}", currentUser.getId());
    }

    /**
     * Đánh dấu 1 THÔNG BÁO cụ thể là đã đọc
     */
    @Transactional
    public void markOneAsRead(Long notiId) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) throw new RuntimeException("Vui lòng đăng nhập!");

        notificationRepository.markOneAsRead(notiId, currentUser.getId());
    }

    /**
     * Xóa mềm 1 thông báo
     */
    @Transactional
    public void deleteNotification(Long notiId) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) throw new RuntimeException("Vui lòng đăng nhập!");

        notificationRepository.softDeleteNotification(notiId, currentUser.getId());
    }

    /**
     * Map Entity sang Response DTO
     */
    private NotificationResponse mapToResponse(Notification noti) {
        return NotificationResponse.builder()
                .id(noti.getId())
                .title(noti.getTitle())
                .message(noti.getMessage())
                .type(noti.getType())
                .relatedId(noti.getRelatedId())
                .senderId(noti.getSenderId())
                .senderAvatar(noti.getSenderAvatar())
                .isRead(noti.isRead())
                .createdAt(noti.getCreatedAt())
                .build();
    }
}