package com.xuanthi.talentmatchingbe.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_noti_user_unread", columnList = "user_id, is_read"),
        @Index(name = "idx_noti_created", columnList = "created_at")
})
@Getter // ✅ Dùng Getter thay cho @Data
@Setter // ✅ Dùng Setter thay cho @Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull(message = "User không được null")
    private User user; // Người nhận thông báo

    @Column(name = "sender_id")
    private Long senderId; // ID người gửi (có thể null nếu là hệ thống)

    @Column(name = "sender_avatar", columnDefinition = "TEXT")
    @Size(max = 500, message = "Avatar URL không được quá 500 ký tự")
    private String senderAvatar; // Avatar để hiện ở chuông thông báo

    @Column(nullable = false)
    @NotBlank(message = "Tiêu đề thông báo không được để trống")
    @Size(min = 3, max = 255, message = "Tiêu đề phải từ 3-255 ký tự")
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    @NotBlank(message = "Nội dung thông báo không được để trống")
    @Size(min = 5, max = 2000, message = "Nội dung phải từ 5-2000 ký tự")
    private String message;

    @Column(length = 50, nullable = false)
    @NotBlank(message = "Loại thông báo không được để trống")
    private String type; // PROFILE_VIEW, APPLICATION, MATCHING, SYSTEM

    @Column(name = "related_id")
    private Long relatedId; // ID của Application/Job để nhấn vào là mở đúng trang

    @Builder.Default
    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;
}