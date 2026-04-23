package com.xuanthi.talentmatchingbe.controller;

import com.xuanthi.talentmatchingbe.dto.chat.ChatMessageDTO;
import com.xuanthi.talentmatchingbe.dto.chat.TypingDTO;
import com.xuanthi.talentmatchingbe.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;

import java.security.Principal;
import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Chat Controller", description = "Các API liên quan chat real - time")
public class ChatController {

    // Đây là "khẩu súng" dùng để bắn tin nhắn về cho Frontend
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;

    /**
     * Frontend sẽ gửi tin nhắn vào đích: /app/chat
     */
    @Operation(summary = "Gửi tin nhắn vào đích: /app/chat")
    @MessageMapping("/chat")
    public void processMessage(@Payload ChatMessageDTO chatMessage, Principal principal) {
        if (principal == null || !StringUtils.hasText(principal.getName())) {
            log.warn("Principal null hoặc không có name trong processMessage");
            return;
        }

        String senderEmail = principal.getName();
        chatMessage.setSenderEmail(senderEmail);

        if (!StringUtils.hasText(chatMessage.getReceiverEmail()) ||
            !StringUtils.hasText(chatMessage.getContent())) {
            log.warn("ChatMessage không hợp lệ từ {}", senderEmail);
            return;
        }

        // 🔥 ĐÃ SỬA Ở ĐÂY: Đổi setTimestamp thành setCreatedAt
        chatMessage.setCreatedAt(LocalDateTime.now());

        try {
            // GỌI SERVICE LƯU VÀO DATABASE BỞI DÒNG NÀY:
            ChatMessageDTO savedMessage = chatService.saveMessage(chatMessage);

            // Bắn tin nhắn đã được gắn ConversationID cho người nhận
            messagingTemplate.convertAndSendToUser(
                    savedMessage.getReceiverEmail(),
                    "/queue/messages",
                    savedMessage
            );
            log.debug("Gửi message real-time từ {} tới {}", senderEmail, savedMessage.getReceiverEmail());
            // 2️⃣ 🚀 Bắn ngược tin nhắn (đã có ID DB) lại cho CHÍNH NGƯỜI GỬI (Bổ sung mới)
            messagingTemplate.convertAndSendToUser(
                    senderEmail,
                    "/queue/messages",
                    savedMessage
            );
            log.debug("Gửi trả message real-time tới NGƯỜI GỬI: {}", senderEmail);
        } catch (Exception e) {
            log.error("Lỗi xử lý message từ {}: {}", senderEmail, e.getMessage());
            // Có thể gửi error message về client nếu cần
        }
    }

    /**
     * Frontend gửi trạng thái đang gõ vào: /app/chat.typing
     */
    @Operation(summary = "Gửi trạng thái đang gõ vào: /app/chat.typing")
    @MessageMapping("/chat.typing")
    public void processTypingStatus(@Payload TypingDTO typingDTO, Principal principal) {
        if (principal == null || typingDTO == null || !StringUtils.hasText(typingDTO.getReceiverEmail())) {
            log.warn("TypingDTO không hợp lệ");
            return;
        }

        // Bắn trạng thái gõ phím tới đúng kênh "/queue/typing" của người nhận
        messagingTemplate.convertAndSendToUser(
                typingDTO.getReceiverEmail(),
                "/queue/typing",
                typingDTO
        );
        log.debug("Gửi typing status tới {}", typingDTO.getReceiverEmail());
    }
}