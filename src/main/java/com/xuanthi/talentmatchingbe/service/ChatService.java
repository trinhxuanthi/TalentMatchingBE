package com.xuanthi.talentmatchingbe.service;

import com.xuanthi.talentmatchingbe.dto.chat.ChatMessageDTO;
import com.xuanthi.talentmatchingbe.dto.chat.ConversationDTO;
import com.xuanthi.talentmatchingbe.entity.Conversation;
import com.xuanthi.talentmatchingbe.entity.Message;
import com.xuanthi.talentmatchingbe.entity.User;
import com.xuanthi.talentmatchingbe.enums.Role;
import com.xuanthi.talentmatchingbe.repository.ConversationRepository;
import com.xuanthi.talentmatchingbe.repository.MessageRepository;
import com.xuanthi.talentmatchingbe.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    @Transactional
    public ChatMessageDTO saveMessage(ChatMessageDTO chatMessageDTO) {
        // 1. Tìm người gửi và người nhận
        User sender = userRepository.findByEmail(chatMessageDTO.getSenderEmail())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người gửi"));
        User receiver = userRepository.findByEmail(chatMessageDTO.getReceiverEmail())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người nhận"));

        // 🔥 BẢO VỆ DỮ LIỆU: Ngăn chặn 2 người cùng Role tạo phòng chat bậy bạ
        if (sender.getRole() == receiver.getRole()) {
            throw new RuntimeException("Lỗi Logic: Hai người dùng cùng vai trò không thể tạo phòng chat!");
        }

        // 2. Xác định phòng chat (Tìm phòng cũ hoặc tạo phòng mới)
        Conversation conversation;
        if (chatMessageDTO.getConversationId() != null) {
            conversation = conversationRepository.findById(chatMessageDTO.getConversationId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng chat"));
        } else {
            // Nếu chưa có ID phòng chat, thử tìm xem 2 người này đã chat chưa
            conversation = conversationRepository.findExistingConversation(sender.getId(), receiver.getId())
                    .orElseGet(() -> {
                        // Nếu chưa từng chat -> Tạo phòng chat mới
                        Conversation newConv = new Conversation();
                        if (sender.getRole() == Role.EMPLOYER) {
                            newConv.setEmployer(sender);
                            newConv.setCandidate(receiver);
                        } else {
                            newConv.setEmployer(receiver);
                            newConv.setCandidate(sender);
                        }
                        return conversationRepository.save(newConv);
                    });
        }

        // 3. Tạo và lưu Tin nhắn (Message)
        Message message = Message.builder()
                .conversation(conversation)
                .sender(sender)
                .content(chatMessageDTO.getContent())
                .isRead(false)
                .build();
        message = messageRepository.save(message);

        // 4. Cập nhật "Tin nhắn cuối cùng" cho Phòng chat
        conversation.setLastMessage(chatMessageDTO.getContent());
        conversationRepository.save(conversation);

        // 5. Trả về DTO đã được gắn thêm thông tin để Frontend hiển thị real-time
        chatMessageDTO.setId(message.getId());
        chatMessageDTO.setConversationId(conversation.getId());
        chatMessageDTO.setSenderId(sender.getId());
        chatMessageDTO.setCreatedAt(message.getCreatedAt());
        return chatMessageDTO;
    }

    // ==============================================================
    // 🔥 FIX LỖI JSON: DÙNG DTO CHO API REST ĐỂ TỐI ƯU TỐC ĐỘ
    // ==============================================================

    @Transactional(readOnly = true)
    public List<ConversationDTO> getMyConversations(Long userId) {
        List<Conversation> conversations = conversationRepository.findAllByUserId(userId);

        return conversations.stream().map(conv -> {
            // Xác định "Đối tác" chat của mình là ai để lấy tên và ảnh đại diện
            User partner = conv.getEmployer().getId().equals(userId) ? conv.getCandidate() : conv.getEmployer();

            return ConversationDTO.builder()
                    .id(conv.getId())
                    .partnerId(partner.getId())
                    .partnerName(partner.getFullName()) // Lấy tên người chat cùng
                    .partnerAvatar(partner.getAvatar()) // Lấy ảnh người chat cùng
                    .lastMessage(conv.getLastMessage())
                    .updatedAt(conv.getUpdatedAt())     // Thời gian tin nhắn cuối
                    .build();
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ChatMessageDTO> getChatHistory(Long conversationId) {
        List<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);

        // Chỉ lấy những thứ cần thiết, bỏ lồng ghép Entity
        return messages.stream().map(msg -> ChatMessageDTO.builder()
                .id(msg.getId())
                .conversationId(msg.getConversation().getId())
                .senderId(msg.getSender().getId())
                .senderEmail(msg.getSender().getEmail())
                .content(msg.getContent())
                .isRead(msg.isRead()) // Hoặc getIsRead() tuỳ bro đặt tên biến boolean
                .createdAt(msg.getCreatedAt())
                .build()
        ).collect(Collectors.toList());
    }

    @Transactional
    public void markConversationAsRead(Long conversationId, Long myUserId) {
        messageRepository.markMessagesAsRead(conversationId, myUserId);
    }

    @Transactional(readOnly = true)
    public long getTotalUnreadCount(Long myUserId) {
        return messageRepository.countTotalUnreadMessages(myUserId);
    }
}