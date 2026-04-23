package com.xuanthi.talentmatchingbe.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entity đại diện cho conversation (cuộc trò chuyện)
 * Quản lý các cuộc chat giữa employer và candidate
 */
@Entity
@Table(name = "conversations",
       indexes = {
           @Index(name = "idx_conversation_employer", columnList = "employer_id"),
           @Index(name = "idx_conversation_candidate", columnList = "candidate_id"),
           @Index(name = "idx_conversation_job", columnList = "job_id"),
           @Index(name = "idx_conversation_updated", columnList = "updated_at")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conversation {

    /**
     * ID duy nhất của conversation
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Employer tham gia conversation
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employer_id")
    private User employer;

    /**
     * Candidate tham gia conversation
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id")
    private User candidate;

    /**
     * Job liên quan đến conversation (optional)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id")
    private Job job;

    /**
     * Nội dung tin nhắn cuối cùng
     */
    @Size(max = 1000, message = "Last message không được vượt quá 1000 ký tự")
    @Column(name = "last_message")
    private String lastMessage;

    /**
     * Thời gian cập nhật cuối cùng
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}