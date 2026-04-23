package com.xuanthi.talentmatchingbe.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity đại diện cho message (tin nhắn)
 * Lưu trữ các tin nhắn trong conversation
 */
@Entity
@Table(name = "messages",
       indexes = {
           @Index(name = "idx_message_conversation", columnList = "conversation_id"),
           @Index(name = "idx_message_sender", columnList = "sender_id"),
           @Index(name = "idx_message_created", columnList = "created_at")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

    /**
     * ID duy nhất của message
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Conversation chứa message này
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id")
    private Conversation conversation;

    /**
     * Người gửi message
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;

    /**
     * Nội dung tin nhắn
     */
    @NotBlank(message = "Nội dung tin nhắn không được rỗng")
    @Size(max = 2000, message = "Nội dung tin nhắn không được vượt quá 2000 ký tự")
    private String content;

    /**
     * Trạng thái đã đọc: true = đã đọc, false = chưa đọc
     */
    @Column(name = "is_read")
    @Builder.Default
    private boolean isRead = false;

    /**
     * Thời gian gửi message
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}