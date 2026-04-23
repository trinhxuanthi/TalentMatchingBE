package com.xuanthi.talentmatchingbe.controller;

import com.xuanthi.talentmatchingbe.dto.chat.ChatMessageDTO;
import com.xuanthi.talentmatchingbe.dto.chat.ConversationDTO;
import com.xuanthi.talentmatchingbe.entity.User;
import com.xuanthi.talentmatchingbe.service.ChatService;
import com.xuanthi.talentmatchingbe.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Chat API", description = "Hệ thống nhắn tin Real-time giữa HR và Ứng viên")
public class ChatRestController {

    private final ChatService chatService;

    // ==========================================
    // 1. KHỞI TẠO PHÒNG CHAT (DÙNG KHI BẤM NÚT "NHẮN TIN")
    // ==========================================
    @Operation(summary = "Khởi tạo hoặc lấy phòng chat với một người khác")
    @PostMapping("/conversations/with/{recipientId}")
    public ResponseEntity<ConversationDTO> getOrCreateConversation(@PathVariable Long recipientId) {
        User currentUser = SecurityUtils.getRequiredCurrentUser();

        log.info("[Chat] User {} is initiating chat with recipient ID: {}", currentUser.getId(), recipientId);
        ConversationDTO conversation = chatService.getOrCreateConversation(currentUser.getId(), recipientId);
        return ResponseEntity.ok(conversation);
    }

    // ==========================================
    // 2. DANH SÁCH CUỘC HỘI THOẠI
    // ==========================================
    @Operation(summary = "Lấy danh sách các phòng chat của tôi (Khung danh sách bên trái)")
    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationDTO>> getMyConversations() {
        User currentUser = SecurityUtils.getRequiredCurrentUser();

        log.debug("[Chat] Fetching conversations for user: {}", currentUser.getEmail());
        List<ConversationDTO> conversations = chatService.getMyConversations(currentUser.getId());
        return ResponseEntity.ok(conversations);
    }

    // ==========================================
    // 3. LỊCH SỬ TIN NHẮN (ĐÃ FIX LỖI THAM SỐ & PHÂN TRANG)
    // ==========================================
    @Operation(summary = "Lấy lịch sử tin nhắn của 1 phòng chat (Có phân trang chống lag)")
    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<Page<ChatMessageDTO>> getChatHistory(
            @PathVariable Long conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.debug("[Chat] Fetching messages for conversation: {} (Page: {}, Size: {})", conversationId, page, size);

        // Gọi Service với đủ 3 tham số để tránh lỗi biên dịch
        Page<ChatMessageDTO> messages = chatService.getChatHistory(conversationId, page, size);
        return ResponseEntity.ok(messages);
    }

    // ==========================================
    // 4. TIỆN ÍCH CHAT
    // ==========================================
    @Operation(summary = "Đánh dấu tất cả tin nhắn trong phòng là Đã đọc")
    @PutMapping("/conversations/{conversationId}/read")
    public ResponseEntity<Map<String, String>> markAsRead(@PathVariable Long conversationId) {
        User currentUser = SecurityUtils.getRequiredCurrentUser();

        chatService.markConversationAsRead(conversationId, currentUser.getId());
        return ResponseEntity.ok(Map.of("message", "Đã đánh dấu đọc!"));
    }

    @Operation(summary = "Lấy tổng số tin nhắn chưa đọc của toàn hệ thống")
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount() {
        User currentUser = SecurityUtils.getRequiredCurrentUser();

        long count = chatService.getTotalUnreadCount(currentUser.getId());
        return ResponseEntity.ok(count);
    }
}