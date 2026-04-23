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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    // ==============================================================
    // 1. TÍNH NĂNG MỚI: KHỞI TẠO HOẶC LẤY PHÒNG CHAT
    // ==============================================================
    @Transactional
    public ConversationDTO getOrCreateConversation(Long myUserId, Long partnerId) {
        User me = userRepository.findById(myUserId).orElseThrow(() -> new RuntimeException("Lỗi xác thực"));
        User partner = userRepository.findById(partnerId).orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        if (me.getRole() == partner.getRole()) {
            throw new RuntimeException("Hai người dùng cùng vai trò không thể chat với nhau!");
        }

        Conversation conv = conversationRepository.findExistingConversation(myUserId, partnerId)
                .orElseGet(() -> {
                    Conversation newConv = new Conversation();
                    if (me.getRole() == Role.EMPLOYER) {
                        newConv.setEmployer(me);
                        newConv.setCandidate(partner);
                    } else {
                        newConv.setEmployer(partner);
                        newConv.setCandidate(me);
                    }
                    return conversationRepository.save(newConv);
                });

        return ConversationDTO.builder()
                .id(conv.getId())
                .partnerId(partner.getId())
                .partnerName(partner.getFullName())
                .partnerAvatar(partner.getAvatar())
                .partnerEmail(partner.getEmail())
                .lastMessage(conv.getLastMessage())
                .updatedAt(conv.getUpdatedAt())
                .unreadCount(messageRepository.countUnreadInConversation(conv.getId(), myUserId))
                .build();
    }

    // ==============================================================
    // 2. LƯU TIN NHẮN (GIỮ NGUYÊN LOGIC SẾP, TỐI ƯU CODE)
    // ==============================================================
    @Transactional
    public ChatMessageDTO saveMessage(ChatMessageDTO dto) {
        if (dto == null || !StringUtils.hasText(dto.getSenderEmail()) ||
                !StringUtils.hasText(dto.getReceiverEmail()) || !StringUtils.hasText(dto.getContent())) {
            throw new IllegalArgumentException("Dữ liệu tin nhắn không hợp lệ!");
        }

        User sender = userRepository.findByEmail(dto.getSenderEmail()).orElseThrow();
        User receiver = userRepository.findByEmail(dto.getReceiverEmail()).orElseThrow();

        Conversation conversation;
        if (dto.getConversationId() != null) {
            conversation = conversationRepository.findById(dto.getConversationId()).orElseThrow();
        } else {
            conversation = conversationRepository.findExistingConversation(sender.getId(), receiver.getId())
                    .orElseThrow(() -> new RuntimeException("Phòng chat chưa được khởi tạo!"));
        }

        Message message = Message.builder()
                .conversation(conversation)
                .sender(sender)
                .content(dto.getContent())
                .isRead(false)
                .build();
        message = messageRepository.save(message);

        conversation.setLastMessage(dto.getContent());
        conversationRepository.save(conversation);

        dto.setId(message.getId());
        dto.setConversationId(conversation.getId());
        dto.setSenderId(sender.getId());
        dto.setCreatedAt(message.getCreatedAt());
        return dto;
    }

    // ==============================================================
    // 3. DANH SÁCH PHÒNG CHAT (CÓ ĐẾM SỐ TIN CHƯA ĐỌC)
    // ==============================================================
    @Transactional(readOnly = true)
    public List<ConversationDTO> getMyConversations(Long userId) {
        // Dùng hàm Optimized để né lỗi N+1
        List<Conversation> conversations = conversationRepository.findAllByUserIdOptimized(userId);

        return conversations.stream().map(conv -> {
            User partner = conv.getEmployer().getId().equals(userId) ? conv.getCandidate() : conv.getEmployer();
            // Lấy số tin chưa đọc cho TỪNG phòng
            long unread = messageRepository.countUnreadInConversation(conv.getId(), userId);

            return ConversationDTO.builder()
                    .id(conv.getId())
                    .partnerId(partner.getId())
                    .partnerName(partner.getFullName())
                    .partnerAvatar(partner.getAvatar())
                    .partnerEmail(partner.getEmail())
                    .lastMessage(conv.getLastMessage())
                    .updatedAt(conv.getUpdatedAt())
                    .unreadCount(unread) // 🚀 Gắn số vào đây
                    .build();
        }).collect(Collectors.toList());
    }

    // ==============================================================
    // 4. LỊCH SỬ TIN NHẮN (ĐÃ TÍCH HỢP PHÂN TRANG)
    // ==============================================================
    @Transactional(readOnly = true)
    public Page<ChatMessageDTO> getChatHistory(Long conversationId, int page, int size) {
        // Sắp xếp DESC trong DB để lấy tin mới nhất, PageRequest lo phần còn lại
        Pageable pageable = PageRequest.of(page, size);
        Page<Message> messagePage = messageRepository.findMessagesByConversationId(conversationId, pageable);

        if (messagePage.isEmpty()) {
            return Page.empty();
        }

        Conversation conv = messagePage.getContent().get(0).getConversation();
        User employer = conv.getEmployer();
        User candidate = conv.getCandidate();

        return messagePage.map(msg -> {
            User receiver = msg.getSender().getId().equals(employer.getId()) ? candidate : employer;
            return ChatMessageDTO.builder()
                    .id(msg.getId())
                    .conversationId(conv.getId())
                    .senderId(msg.getSender().getId())
                    .senderEmail(msg.getSender().getEmail())
                    .content(msg.getContent())
                    .isRead(msg.isRead())
                    .createdAt(msg.getCreatedAt())
                    .receiverEmail(receiver.getEmail())
                    .build();
        });
    }

    // ==============================================================
    // 5. CÁC HÀM TIỆN ÍCH (ĐÁNH DẤU ĐỌC, ĐẾM TỔNG)
    // ==============================================================
    @Transactional
    public void markConversationAsRead(Long conversationId, Long myUserId) {
        messageRepository.markMessagesAsRead(conversationId, myUserId);
    }

    @Transactional(readOnly = true)
    public long getTotalUnreadCount(Long myUserId) {
        return messageRepository.countTotalUnreadMessages(myUserId);
    }
}