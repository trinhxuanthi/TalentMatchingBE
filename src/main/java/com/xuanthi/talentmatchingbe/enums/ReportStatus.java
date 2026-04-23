package com.xuanthi.talentmatchingbe.enums;

public enum ReportStatus {
    PENDING,        // Vừa gửi lên, Admin chưa đọc (Chờ xử lý)
    PROCESSING,     // Admin đang xem xét/kiểm tra (Đang xử lý)
    RESOLVED,       // Đã xử lý xong (Có thể kèm Admin Note)
    REJECTED        // Báo cáo tào lao, Admin từ chối xử lý
}