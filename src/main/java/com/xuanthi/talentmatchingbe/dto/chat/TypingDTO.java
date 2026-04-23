package com.xuanthi.talentmatchingbe.dto.chat;

import lombok.Data;

/**
 * DTO cho trạng thái typing (đang gõ phím)
 * Sử dụng cho WebSocket real-time chat
 */
@Data
public class TypingDTO {

    /**
     * ID của conversation
     */
    private Long conversationId;

    /**
     * Email của người nhận thông báo typing
     */
    private String receiverEmail;

    /**
     * Trạng thái typing: true = đang gõ, false = ngừng gõ
     */
    private boolean isTyping;
}