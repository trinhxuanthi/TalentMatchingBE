package com.xuanthi.talentmatchingbe.service;

import com.xuanthi.talentmatchingbe.dto.ChatMessageDTO;
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
                        // Gán đúng vai trò (Ai là HR, ai là Candidate)
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
        messageRepository.save(message);

        // 4. Cập nhật "Tin nhắn cuối cùng" và "Thời gian" cho Phòng chat
        conversation.setLastMessage(chatMessageDTO.getContent());
        conversationRepository.save(conversation);

        // 5. Trả về DTO đã được gắn thêm conversationId để gửi cho Frontend
        chatMessageDTO.setConversationId(conversation.getId());
        chatMessageDTO.setSenderId(sender.getId());
        return chatMessageDTO;
    }

    // --- HÀM CHO API REST HTTP BÌNH THƯỜNG ---

    @Transactional(readOnly = true)
    public List<Conversation> getMyConversations(Long userId) {
        return conversationRepository.findAllByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<Message> getChatHistory(Long conversationId) {
        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
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