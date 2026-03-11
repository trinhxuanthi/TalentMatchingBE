package com.xuanthi.talentmatchingbe.service;

import com.xuanthi.talentmatchingbe.dto.notification.NotificationResponse;
import com.xuanthi.talentmatchingbe.entity.Notification;
import com.xuanthi.talentmatchingbe.entity.User;
import com.xuanthi.talentmatchingbe.repository.NotificationRepository;
import com.xuanthi.talentmatchingbe.repository.UserRepository;
import com.xuanthi.talentmatchingbe.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    /**
     * HÀM GỬI THÔNG BÁO (Dùng nội bộ hệ thống)
     * Có thể gọi từ bất kỳ Service nào khác
     */
    @Transactional
    public void sendNotification(Long receiverId, Long senderId, String title, String message, String type, Long relatedId) {
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Người nhận không tồn tại"));

        String senderAvatar = null;
        if (senderId != null) {
            senderAvatar = userRepository.findById(senderId)
                    .map(User::getAvatar).orElse(null);
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

        notificationRepository.save(noti);
    }

    /**
     * LẤY DANH SÁCH THÔNG BÁO CỦA TÔI (Phân trang)
     */
    public Page<NotificationResponse> getMyNotifications(int page, int size) {
        User currentUser = SecurityUtils.getCurrentUser();

        // Sử dụng Index idx_noti_user_unread đã tạo ở SQL
        return notificationRepository.findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(
                        currentUser.getId(), PageRequest.of(page, size))
                .map(this::mapToResponse);
    }

    /**
     * ĐÁNH DẤU ĐÃ ĐỌC TẤT CẢ
     */
    @Transactional
    public void markAllRead() {
        User currentUser = SecurityUtils.getCurrentUser();
        notificationRepository.markAllAsRead(currentUser.getId());
    }

    // Hàm phụ map Entity sang Response (Có thể đưa vào Mapper nếu muốn)
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