package com.xuanthi.talentmatchingbe.dto.notification;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {
    private Long id;
    private String title;
    private String message;
    private String type;
    private Long relatedId;
    private Long senderId;
    private String senderAvatar;
    private boolean isRead;
    private LocalDateTime createdAt;
}
