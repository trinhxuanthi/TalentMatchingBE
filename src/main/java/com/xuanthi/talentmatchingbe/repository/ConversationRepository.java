package com.xuanthi.talentmatchingbe.repository;

import com.xuanthi.talentmatchingbe.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    // Lấy tất cả phòng chat của 1 User (Dù họ là HR hay Candidate) - Sắp xếp tin nhắn mới nhất lên đầu
    @Query("SELECT c FROM Conversation c WHERE c.employer.id = :userId OR c.candidate.id = :userId ORDER BY c.updatedAt DESC")
    List<Conversation> findAllByUserId(@Param("userId") Long userId);

    // Kiểm tra xem 2 người này đã từng chat với nhau chưa
    @Query("SELECT c FROM Conversation c WHERE (c.employer.id = :user1 AND c.candidate.id = :user2) OR (c.employer.id = :user2 AND c.candidate.id = :user1)")
    Optional<Conversation> findExistingConversation(@Param("user1") Long user1Id, @Param("user2") Long user2Id);
}