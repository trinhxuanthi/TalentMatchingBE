package com.xuanthi.talentmatchingbe.repository;

import com.xuanthi.talentmatchingbe.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    // Lấy lịch sử tin nhắn của 1 phòng chat, sắp xếp theo thời gian cũ -> mới (để render từ trên xuống dưới)
    List<Message> findByConversationIdOrderByCreatedAtAsc(Long conversationId);

    // 1. Chuyển tất cả tin nhắn của NGƯỜI KIA gửi cho MÌNH thành Đã Đọc
    @Modifying // Bắt buộc phải có khi dùng lệnh UPDATE trong Spring Data JPA
    @Query("UPDATE Message m SET m.isRead = true WHERE m.conversation.id = :conversationId AND m.sender.id != :myUserId AND m.isRead = false")
    void markMessagesAsRead(@Param("conversationId") Long conversationId, @Param("myUserId") Long myUserId);

    // 2. Đếm tổng số tin nhắn chưa đọc của TÔI (dùng để hiển thị cục màu đỏ ở thanh Menu)
    @Query("SELECT COUNT(m) FROM Message m WHERE (m.conversation.employer.id = :myUserId OR m.conversation.candidate.id = :myUserId) AND m.sender.id != :myUserId AND m.isRead = false")
    long countTotalUnreadMessages(@Param("myUserId") Long myUserId);
}