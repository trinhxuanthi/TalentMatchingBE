package com.xuanthi.talentmatchingbe.dto.job;

import com.xuanthi.talentmatchingbe.enums.JobType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class JobRequest {

    // ==========================================
    // 1. KHỐI THÔNG TIN CƠ BẢN & LƯƠNG
    // ==========================================
    @NotBlank(message = "Tiêu đề công việc không được để trống")
    private String title;

    @NotBlank(message = "Địa điểm làm việc không được để trống")
    private String location;

    private boolean isSalaryNegotiable; // Cờ: Lương thỏa thuận
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;

    @NotNull(message = "Hạn chót nộp hồ sơ không được để trống")
    private LocalDateTime deadline;

    // ==========================================
    // 2. KHỐI THÔNG TIN SIDEBAR (Lọc & Phân loại)
    // ==========================================
    @NotNull(message = "Hình thức làm việc không được để trống")
    private JobType jobType;

    @NotBlank(message = "Kinh nghiệm yêu cầu không được để trống")
    private String experienceLevel;

    private String jobLevel;       // VD: "Quản lý / Giám sát"
    private String educationLevel; // VD: "Đại học trở lên"

    @Min(value = 1, message = "Số lượng tuyển tối thiểu là 1")
    private Integer quantity;

    // ==========================================
    // 3. KHỐI VĂN BẢN CHI TIẾT (Hiển thị UI)
    // ==========================================
    @NotBlank(message = "Mô tả công việc không được để trống")
    private String description;

    private String benefits; // Có thể null (nếu công ty keo kiệt không có phúc lợi) 😂

    // ==========================================
    // 4. KHỐI DỮ LIỆU "NUÔI" ĐỘNG CƠ AI PYTHON
    // ==========================================
    @NotBlank(message = "Yêu cầu công việc (dành cho AI) không được để trống")
    private String requirements; // Gửi thẳng cho SBERT chấm

    @NotNull(message = "Phải có ít nhất 1 kỹ năng bắt buộc để AI chấm điểm")
    private List<String> mustHaveSkills; // Frontend gửi mảng: ["java", "spring"]

    private List<String> niceToHaveSkills; // Mảng kỹ năng ưu tiên: ["docker", "redis"]

    private List<String> categories; // Mảng danh mục nghề: ["IT", "Backend"]
}