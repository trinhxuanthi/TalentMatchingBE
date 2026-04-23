package com.xuanthi.talentmatchingbe.enums;

public enum ApplicationStatus {
    PENDING,    // 1. Ứng viên vừa nộp (Chưa xem)
    VIEWED,     // 2. HR đã click vào xem chi tiết (Khóa chức năng rút đơn)
    REVIEWING,  // 3. HR đang đánh giá
    INTERVIEW,  // 4. Gọi đi phỏng vấn (Sếp làm bước này cực hay!)
    ACCEPTED,   // 5. Nhận việc
    REJECTED,   // 6. Tịt ngòi
    WITHDRAWN   // 7. Ứng viên "quay xe" tự rút đơn
}