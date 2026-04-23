package com.xuanthi.talentmatchingbe.enums;

public enum ReportType {
    SYSTEM_BUG,     // Lỗi hệ thống (VD: Không tải được CV, App crash)
    USER_REPORT,    // Tố cáo người dùng (VD: Công ty ma, Ứng viên gửi link virus)
    SUGGESTION,     // Góp ý cải thiện tính năng
    OTHER           // Các vấn đề khác
}