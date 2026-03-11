package com.xuanthi.talentmatchingbe.enums;

public enum ApplicationStatus {
    PENDING,    // Mới nộp, chờ nhà tuyển dụng xem
    REVIEWING,  // Đang được xem xét (Nhà tuyển dụng đã mở CV ra xem)
    INTERVIEW,  // Đồng ý gọi phỏng vấn
    ACCEPTED,   // Chấp nhận tuyển dụng (Pass)
    REJECTED,   // Từ chối (Fail)
    CANCELED    // Ứng viên tự rút đơn
}