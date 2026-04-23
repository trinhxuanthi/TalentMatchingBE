package com.xuanthi.talentmatchingbe.repository;

import com.xuanthi.talentmatchingbe.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    // 🔥 TỐI ƯU N+1: Dùng JOIN FETCH để lôi luôn User ra trong 1 câu SQL
    @Query("SELECT c FROM Conversation c JOIN FETCH c.employer JOIN FETCH c.candidate " +
            "WHERE c.employer.id = :userId OR c.candidate.id = :userId " +
            "ORDER BY c.updatedAt DESC")
    List<Conversation> findAllByUserIdOptimized(@Param("userId") Long userId);

    // Tìm xem 2 người đã từng chat với nhau chưa
    @Query("SELECT c FROM Conversation c " +
            "WHERE (c.employer.id = :userId1 AND c.candidate.id = :userId2) " +
            "OR (c.employer.id = :userId2 AND c.candidate.id = :userId1)")
    Optional<Conversation> findExistingConversation(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
}