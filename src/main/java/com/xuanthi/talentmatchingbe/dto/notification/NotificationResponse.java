package com.xuanthi.talentmatchingbe.dto.notification;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) // ✅ Giữ lại dòng này, cực kỳ đáng tiền!
public class NotificationResponse {

    private Long id;
    private String title;
    private String message;
    private String type;

    private Long relatedId;
    private Long senderId;
    private String senderAvatar;

    private boolean isRead; // Jackson sẽ tự map thành "isRead" hoặc "read" tùy phiên bản
    private LocalDateTime createdAt;
}