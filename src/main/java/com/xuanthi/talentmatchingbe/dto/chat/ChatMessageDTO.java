package com.xuanthi.talentmatchingbe.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ChatMessageDTO {
    private Long conversationId; // Chat trong phòng nào?
    private Long senderId;       // Ai gửi?
    private String senderEmail;  // Thêm email để tiện hiển thị
    private String receiverEmail; // BẮT BUỘC CÓ: Để Backend biết đường "bắn" tin nhắn tới ai
    private String content;      // Nội dung chat
    private LocalDateTime timestamp;
}