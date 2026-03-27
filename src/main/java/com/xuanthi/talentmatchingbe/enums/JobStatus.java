package com.xuanthi.talentmatchingbe.enums;

public enum JobStatus {
    // --- NHÓM CỦA HR (NHÀ TUYỂN DỤNG) ---
    DRAFT,      // Bản nháp (HR đang viết dở, chưa lưu công khai)
    OPEN,       // Đang tuyển dụng (Hiển thị ngoài trang chủ)
    PAUSED,     // [MỚI] Tạm dừng (HR nhận quá nhiều CV đọc không kịp nên tạm ẩn vài ngày, sau đó mở lại không cần đăng bài mới)
    CLOSED,     // HR chủ động đóng (Đã tuyển đủ người)

    // --- NHÓM CỦA HỆ THỐNG TỰ ĐỘNG ---
    EXPIRED,    // Tự động đóng do quá hạn deadline
    DELETED,    // [MỚI] Xóa mềm (Giải quyết triệt để lỗi 400 lúc nãy. HR bấm "Xóa", ta chuyển về DELETED để ẩn đi chứ không xóa thật trong DB)

    // --- NHÓM CỦA ADMIN (KIỂM DUYỆT BẢO MẬT) ---
    PENDING,    // [MỚI] Chờ duyệt (Nếu hệ thống của bạn yêu cầu Admin phải đọc tin đăng xem có lừa đảo không rồi mới cho OPEN)
    REJECTED,   // [MỚI] Bị từ chối (Admin từ chối tin đăng do sai quy định, bắt HR sửa lại)
    BANNED      // [MỚI] Bị khóa/Đình chỉ (Tin đang OPEN nhưng bị ứng viên Report lừa đảo đa cấp, Admin giáng đấm khóa ngay lập tức)
}