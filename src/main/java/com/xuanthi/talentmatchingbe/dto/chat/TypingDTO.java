package com.xuanthi.talentmatchingbe.dto.chat;

import lombok.Data;

@Data
public class TypingDTO {
    private Long conversationId;
    private String receiverEmail; // Bắn sự kiện gõ phím cho ai?
    private boolean isTyping;     // true = đang gõ, false = ngừng gõ
}