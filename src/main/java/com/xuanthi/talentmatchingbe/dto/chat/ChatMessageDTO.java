package com.xuanthi.talentmatchingbe.dto.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor // Bắt buộc phải có để Spring Boot đọc được JSON từ Client gửi lên
@AllArgsConstructor // Bắt buộc đi kèm với @Builder
public class ChatMessageDTO {

    private Long id;              // BỔ SUNG: ID của tin nhắn (Frontend rất cần cái này)
    private Long conversationId;  // Chat trong phòng nào?

    private Long senderId;        // Ai gửi?
    private String senderEmail;   // Thêm email để tiện hiển thị

    @NotBlank(message = "Email người nhận không được rỗng")
    private String receiverEmail; // BẮT BUỘC CÓ: Để Backend biết đường "bắn" tin nhắn tới ai

    @NotBlank(message = "Nội dung tin nhắn không được rỗng")
    @Size(max = 1000, message = "Nội dung tin nhắn không được quá 1000 ký tự")
    private String content;       // Nội dung chat

    private boolean isRead;       // BỔ SUNG: Trạng thái Đã đọc / Chưa đọc

    private LocalDateTime createdAt; // ĐỔI TÊN: Cho khớp với Entity và Service (thay vì timestamp)


}