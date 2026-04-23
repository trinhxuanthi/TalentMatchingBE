package com.xuanthi.talentmatchingbe.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "companies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank(message = "Tên công ty không được để trống")
    @Size(max = 200, message = "Tên công ty không được quá 200 ký tự")
    private String name; // Tên công ty

    @Column(name = "tax_code", nullable = false, unique = true)
    @NotBlank(message = "Mã số thuế không được để trống")
    @Size(min = 10, max = 20, message = "Mã số thuế phải từ 10-20 ký tự")
    private String taxCode; // Mã số thuế

    @Column(nullable = false)
    @NotBlank(message = "Địa chỉ không được để trống")
    @Size(max = 255, message = "Địa chỉ không được quá 255 ký tự")
    private String address;

    @Column(nullable = false)
    @NotBlank(message = "Website không được để trống")
    @Size(max = 255, message = "Website không được quá 255 ký tự")
    private String website;

    @Column(name = "business_license_url", nullable = false)
    @NotBlank(message = "URL giấy phép kinh doanh không được để trống")
    @Size(max = 500, message = "URL không được quá 500 ký tự")
    private String businessLicenseUrl; // Ảnh giấy phép KD

    @Column(name = "hr_position", nullable = false)
    @NotBlank(message = "Chức vụ HR không được để trống")
    @Size(max = 100, message = "Chức vụ không được quá 100 ký tự")
    private String hrPosition; // Chức vụ người đăng ký

    // Trạng thái duyệt: PENDING, APPROVED, REJECTED
    @Column(name = "approval_status", nullable = false)
    @NotBlank(message = "Trạng thái duyệt không được để trống")
    @Builder.Default
    private String approvalStatus = "PENDING";

    @Column(name = "reject_reason", columnDefinition = "TEXT")
    @Size(max = 1000, message = "Lý do từ chối không được quá 1000 ký tự")
    private String rejectReason; // Lý do từ chối (nếu có)

    // Liên kết 1-1 với User (Người tạo yêu cầu)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    @NotNull(message = "User không được null")
    private User user;

    // THÊM 2 TRƯỜNG NÀY ĐỂ LÀM GIAO DIỆN TOPCV
    @Column(name = "logo_url", length = 500)
    @Size(max = 500, message = "Logo URL không được quá 500 ký tự")
    private String logoUrl; // Ảnh logo công ty

    @Column(columnDefinition = "TEXT")
    @Size(max = 5000, message = "Mô tả không được quá 5000 ký tự")
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "is_locked", nullable = false)
    private boolean locked = false;
}