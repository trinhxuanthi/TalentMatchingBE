package com.xuanthi.talentmatchingbe.dto.ai;

import com.xuanthi.talentmatchingbe.dto.job.AiWeightDto;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * DTO cho yêu cầu Matching AI (Quick Match)
 * - Chứa danh sách CV URLs cần so khớp
 * - Job Description có thể từ URL hoặc text trực tiếp
 * - Hỗ trợ custom rules để tùy chỉnh điểm số AI
 */
@Data
public class QuickMatchRequest {
    
    /**
     * Danh sách đường dẫn CV URL của các ứng viên (bắt buộc, tối thiểu 1, tối đa 100)
     */
    @NotEmpty(message = "Danh sách CV URLs không được rỗng")
    @Size(min = 1, max = 100, message = "Số lượng CV phải từ 1-100")
    private List<String> cvUrls;
    
    /**
     * URL của Job Description (tùy chọn)
     */
    @Size(max = 2000, message = "URL không được vượt quá 2000 ký tự")
    private String jdUrl;
    
    /**
     * Nội dung Job Description nhập trực tiếp (tùy chọn)
     */
    @Size(max = 5000, message = "Nội dung JD không được vượt quá 5000 ký tự")
    private String jdText;
    
    /**
     * Các quy tắc tùy chỉnh cho AI (tùy chọn)
     * Ví dụ: "Kỹ năng bắt buộc: Java, Spring Boot"
     */
    @Size(max = 2000, message = "Custom rules không được vượt quá 2000 ký tự")
    private String customRules;
    
    /**
     * ID của Job trong hệ thống (tùy chọn)
     * Nếu cung cấp, sẽ lấy thông tin Job từ DB để bổ sung vào JD
     */
    private Long jobId;

    private AiWeightDto aiSettings;
}