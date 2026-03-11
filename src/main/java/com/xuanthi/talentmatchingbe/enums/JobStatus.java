package com.xuanthi.talentmatchingbe.enums;

public enum JobStatus {
    OPEN,       // Đang mở tuyển dụng
    CLOSED,     // Chủ động đóng (đã tìm được người)
    EXPIRED,    // Tự động đóng do hết hạn deadline
    DRAFT       // Bản nháp (chưa công khai)
}
