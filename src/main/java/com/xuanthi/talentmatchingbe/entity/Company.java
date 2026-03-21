package com.xuanthi.talentmatchingbe.entity;

import jakarta.persistence.*;
import lombok.*;

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
    private String name; // Tên công ty

    @Column(name = "tax_code", nullable = false)
    private String taxCode; // Mã số thuế

    private String address;
    private String website;

    @Column(name = "business_license_url")
    private String businessLicenseUrl; // Ảnh giấy phép KD

    @Column(name = "hr_position")
    private String hrPosition; // Chức vụ người đăng ký

    // Trạng thái duyệt: PENDING, APPROVED, REJECTED
    @Column(name = "approval_status")
    private String approvalStatus;

    @Column(name = "reject_reason")
    private String rejectReason; // Lý do từ chối (nếu có)

    // Liên kết 1-1 với User (Người tạo yêu cầu)
    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    // THÊM 2 TRƯỜNG NÀY ĐỂ LÀM GIAO DIỆN TOPCV
    @Column(name = "logo_url", length = 500)
    private String logoUrl; // Ảnh logo công ty

    @Column(columnDefinition = "TEXT")
    private String description;
}