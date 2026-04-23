package com.xuanthi.talentmatchingbe.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * DTO cho thông tin conversation (cuộc trò chuyện)
 * Sử dụng để hiển thị danh sách chat
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationDTO {

    /**
     * ID của conversation
     */
    private Long id;

    /**
     * ID của người đang chat cùng mình
     */
    private Long partnerId;

    /**
     * Tên hiển thị của người chat cùng
     */
    private String partnerName;

    /**
     * Ảnh đại diện của người chat cùng
     */
    private String partnerAvatar;

    /**
     * Nội dung tin nhắn cuối cùng
     */
    private String lastMessage;

    /**
     * Thời gian cập nhật cuối cùng
     */
    private LocalDateTime updatedAt;

    /**
     * Email của người chat cùng
     */
    private String partnerEmail;

    private long unreadCount;
}