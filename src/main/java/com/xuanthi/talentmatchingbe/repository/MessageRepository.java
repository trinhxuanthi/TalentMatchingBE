package com.xuanthi.talentmatchingbe.repository;

import com.xuanthi.talentmatchingbe.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MessageRepository extends JpaRepository<Message, Long> {

    // 🔥 TỐI ƯU: Phân trang + JOIN FETCH Sender (Sắp xếp DESC để lấy tin mới nhất trước)
    @Query("SELECT m FROM Message m JOIN FETCH m.sender " +
            "WHERE m.conversation.id = :conversationId " +
            "ORDER BY m.createdAt DESC")
    Page<Message> findMessagesByConversationId(@Param("conversationId") Long conversationId, Pageable pageable);

    // Đánh dấu đã đọc (Chỉ update những tin do NGƯỜI KHÁC gửi cho mình)
    @Modifying
    @Query("UPDATE Message m SET m.isRead = true " +
            "WHERE m.conversation.id = :conversationId AND m.sender.id != :myUserId AND m.isRead = false")
    void markMessagesAsRead(@Param("conversationId") Long conversationId, @Param("myUserId") Long myUserId);

    // Đếm tin nhắn chưa đọc TRONG 1 PHÒNG CỤ THỂ
    @Query("SELECT COUNT(m) FROM Message m " +
            "WHERE m.conversation.id = :conversationId AND m.sender.id != :myUserId AND m.isRead = false")
    long countUnreadInConversation(@Param("conversationId") Long conversationId, @Param("myUserId") Long myUserId);

    // Đếm TỔNG SỐ tin nhắn chưa đọc của toàn bộ hệ thống (Cho icon cái chuông)
    @Query("SELECT COUNT(m) FROM Message m JOIN m.conversation c " +
            "WHERE (c.employer.id = :myUserId OR c.candidate.id = :myUserId) " +
            "AND m.sender.id != :myUserId AND m.isRead = false")
    long countTotalUnreadMessages(@Param("myUserId") Long myUserId);
}