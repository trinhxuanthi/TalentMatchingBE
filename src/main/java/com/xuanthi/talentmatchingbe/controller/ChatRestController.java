package com.xuanthi.talentmatchingbe.controller;

import com.xuanthi.talentmatchingbe.dto.chat.ChatMessageDTO;
import com.xuanthi.talentmatchingbe.dto.chat.ConversationDTO;
import com.xuanthi.talentmatchingbe.service.ChatService;
import com.xuanthi.talentmatchingbe.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Tag(name = "ChatRestController", description = "Các API liên quan chat thông thường")
public class ChatRestController {

    private final ChatService chatService;

    // 1. Lấy danh sách các phòng chat của tôi (Khung bên trái giao diện chat)
    @Operation(summary = "Lấy danh sách các phòng chat của tôi (Khung bên trái giao diện chat)")
    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationDTO>> getMyConversations() {
        Long myUserId = SecurityUtils.getCurrentUser().getId();
        // chatService giờ đã trả về List<ConversationDTO> sạch sẽ
        return ResponseEntity.ok(chatService.getMyConversations(myUserId));
    }

    // 2. Lấy lịch sử tin nhắn của 1 phòng chat cụ thể (Khung bên phải giao diện chat)
    @Operation(summary = "Lấy lịch sử tin nhắn của 1 phòng chat cụ thể (Khung bên phải giao diện chat)")
    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<List<ChatMessageDTO>> getChatHistory(@PathVariable Long conversationId) {
        // chatService giờ đã trả về List<ChatMessageDTO> gọn nhẹ
        return ResponseEntity.ok(chatService.getChatHistory(conversationId));
    }
    // 3. API để Đánh dấu Đã Đọc (Frontend gọi API này khi User click vào một phòng chat)
    @Operation(summary = "API để Đánh dấu Đã Đọc (Frontend gọi API này khi User click vào một phòng chat)")
    @PutMapping("/conversations/{conversationId}/read")
    public ResponseEntity<String> markAsRead(@PathVariable Long conversationId) {
        Long myUserId = SecurityUtils.getCurrentUser().getId();
        chatService.markConversationAsRead(conversationId, myUserId);
        return ResponseEntity.ok("Đã đánh dấu đọc!");
    }

    // 4. API lấy Tổng số tin nhắn chưa đọc (Frontend gọi API này khi vừa load trang web xong)
    @Operation(summary = "API lấy Tổng số tin nhắn chưa đọc (Frontend gọi API này khi vừa load trang web xong)")
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount() {
        Long myUserId = SecurityUtils.getCurrentUser().getId();
        return ResponseEntity.ok(chatService.getTotalUnreadCount(myUserId));
    }
}