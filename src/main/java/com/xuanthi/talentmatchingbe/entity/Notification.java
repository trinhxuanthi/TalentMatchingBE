package com.xuanthi.talentmatchingbe.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Người nhận thông báo

    @Column(name = "sender_id")
    private Long senderId; // ID người gửi (có thể null nếu là hệ thống)

    @Column(name = "sender_avatar", columnDefinition = "TEXT")
    private String senderAvatar; // Avatar để hiện ở chuông thông báo

    @NotBlank(message = "Tiêu đề thông báo không được để trống")
    @Column(nullable = false)
    private String title;

    @NotBlank(message = "Nội dung thông báo không được để trống")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    @Column(length = 50)
    private String type; // APPLICATION, MATCHING, SYSTEM

    @Column(name = "related_id")
    private Long relatedId; // ID của Application/Job để nhấn vào là mở đúng trang

    @Builder.Default
    @Column(name = "is_read")
    private boolean isRead = false;

    @Builder.Default
    @Column(name = "is_deleted")
    private boolean isDeleted = false;

    @CreationTimestamp // TỰ ĐỘNG: Hibernate tự điền thời gian khi insert
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}