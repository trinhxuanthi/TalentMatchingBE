package com.xuanthi.talentmatchingbe.controller;

import com.xuanthi.talentmatchingbe.dto.ChatMessageDTO;
import com.xuanthi.talentmatchingbe.dto.chat.TypingDTO;
import com.xuanthi.talentmatchingbe.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
@Tag(name = "ChatController", description = "Các API liên quan chat real - time")
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
        String senderEmail = principal.getName();
        chatMessage.setSenderEmail(senderEmail);
        chatMessage.setTimestamp(LocalDateTime.now());

        // GỌI SERVICE LƯU VÀO DATABASE BỞI DÒNG NÀY:
        ChatMessageDTO savedMessage = chatService.saveMessage(chatMessage);

        // Bắn tin nhắn đã được gắn ConversationID cho người nhận
        messagingTemplate.convertAndSendToUser(
                savedMessage.getReceiverEmail(),
                "/queue/messages",
                savedMessage
        );
    }

    /**
     * Frontend gửi trạng thái đang gõ vào: /app/chat.typing
     */
    @Operation(summary = "Gửi trạng thái đang gõ vào: /app/chat.typing")
    @MessageMapping("/chat.typing")
    public void processTypingStatus(@Payload TypingDTO typingDTO, Principal principal) {
        // Bắn trạng thái gõ phím tới đúng kênh "/queue/typing" của người nhận
        messagingTemplate.convertAndSendToUser(
                typingDTO.getReceiverEmail(),
                "/queue/typing",
                typingDTO
        );
    }
}