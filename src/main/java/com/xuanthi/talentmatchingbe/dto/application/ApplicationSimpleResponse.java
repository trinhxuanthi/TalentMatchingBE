package com.xuanthi.talentmatchingbe.dto.application;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.xuanthi.talentmatchingbe.enums.ApplicationStatus;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO đơn giản cho response của application
 * Chứa thông tin cơ bản để hiển thị trong danh sách
 */
@Builder
@Data
public class ApplicationSimpleResponse {

    /**
     * ID của đơn ứng tuyển
     */
    @Positive(message = "ID phải là số dương")
    private Long id;

    /**
     * ID của ứng viên
     */
    @Positive(message = "Candidate ID phải là số dương")
    private Long candidateId;

    /**
     * Họ tên ứng viên
     */
    @Size(max = 100, message = "Họ tên không được vượt quá 100 ký tự")
    private String candidateFullName;

    /**
     * Email ứng viên
     */
    @Email(message = "Email không đúng định dạng")
    @Size(max = 191, message = "Email không được vượt quá 191 ký tự")
    private String candidateEmail;

    /**
     * URL CV
     */
    @Pattern(regexp = "^https?://.*", message = "CV URL phải là URL hợp lệ")
    @Size(max = 500, message = "CV URL không được vượt quá 500 ký tự")
    private String cvUrl;

    /**
     * Điểm số AI matching
     */
    @Min(value = 0, message = "Match score không được âm")
    @Max(value = 100, message = "Match score không được vượt quá 100")
    private Integer matchScore;

    /**
     * Khuyến nghị của AI
     */
    @Size(max = 100, message = "AI recommendation không được vượt quá 100 ký tự")
    private String aiRecommendation;

    /**
     * Trạng thái đơn ứng tuyển
     */
    private ApplicationStatus status;

    /**
     * Thời gian nộp đơn
     */
    private LocalDateTime appliedAt;

    // Trong ApplicationResponse.java
    @JsonProperty("isCandidatePro")
    private boolean isCandidatePro;
}