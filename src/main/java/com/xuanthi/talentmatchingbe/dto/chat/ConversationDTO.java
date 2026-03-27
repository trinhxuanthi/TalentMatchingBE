package com.xuanthi.talentmatchingbe.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationDTO {
    private Long id;
    private Long partnerId;      // ID của người đang chat cùng mình
    private String partnerName;  // Tên hiển thị của họ
    private String partnerAvatar;// Ảnh đại diện
    private String lastMessage;
    private LocalDateTime updatedAt;
}