package com.xuanthi.talentmatchingbe.service;

import com.xuanthi.talentmatchingbe.dto.company.CompanyDetailResponse;
import com.xuanthi.talentmatchingbe.dto.company.CompanyResponse;
import com.xuanthi.talentmatchingbe.dto.user.EmployerUpgradeRequest;
import com.xuanthi.talentmatchingbe.entity.Company;
import com.xuanthi.talentmatchingbe.entity.User;
import com.xuanthi.talentmatchingbe.enums.Role;
import com.xuanthi.talentmatchingbe.repository.CompanyRepository;
import com.xuanthi.talentmatchingbe.repository.UserRepository;
import com.xuanthi.talentmatchingbe.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService; // Nếu bạn có hệ thống thông báo

    // ==========================================
    // 1. NGHIỆP VỤ CỦA ỨNG VIÊN (CANDIDATE)
    // ==========================================
    @Transactional
    public String requestUpgradeToEmployer(EmployerUpgradeRequest request) {
        User currentUser = SecurityUtils.getCurrentUser();

        // Kiểm tra xem đã gửi yêu cầu chưa
        Optional<Company> existingCompany = companyRepository.findByUserId(currentUser.getId());
        if (existingCompany.isPresent()) {
            String status = existingCompany.get().getApprovalStatus();
            if ("PENDING".equals(status)) {
                throw new RuntimeException("Hồ sơ công ty của bạn đang được xét duyệt!");
            } else if ("APPROVED".equals(status)) {
                throw new RuntimeException("Bạn đã là Nhà tuyển dụng rồi!");
            }
            // Nếu bị REJECTED thì cho phép nộp lại ở bên dưới
        }

        // Kiểm tra trùng Mã số thuế
        if (companyRepository.existsByTaxCode(request.getTaxCode()) &&
                (existingCompany.isEmpty() || !existingCompany.get().getTaxCode().equals(request.getTaxCode()))) {
            throw new RuntimeException("Mã số thuế này đã được đăng ký bởi tài khoản khác!");
        }

        // Tạo mới hoặc ghi đè hồ sơ cũ (nếu từng bị từ chối)
        Company company = existingCompany.orElse(new Company());
        company.setName(request.getCompanyName());
        company.setTaxCode(request.getTaxCode());
        company.setAddress(request.getCompanyAddress());
        company.setWebsite(request.getWebsite());
        company.setBusinessLicenseUrl(request.getBusinessLicenseUrl());
        company.setHrPosition(request.getPosition());
        company.setApprovalStatus("PENDING");
        company.setUser(currentUser);
        company.setRejectReason(null); // Xóa lý do từ chối cũ nếu có

        companyRepository.save(company);
        return "Nộp hồ sơ thành công! Vui lòng chờ Admin xét duyệt.";
    }

    // ==========================================
    // 2. NGHIỆP VỤ CỦA QUẢN TRỊ VIÊN (ADMIN)
    // ==========================================
    @Transactional(readOnly = true)
    public List<Company> getPendingCompanies() {
        return companyRepository.findByApprovalStatus("PENDING");
    }

    @Transactional
    public String approveCompany(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hồ sơ công ty!"));

        company.setApprovalStatus("APPROVED");
        companyRepository.save(company);

        // Nâng cấp Role cho User
        User user = company.getUser();
        user.setRole(Role.EMPLOYER);
        userRepository.save(user);

        // (Tùy chọn) Gửi thông báo
        if (notificationService != null) {
            notificationService.sendNotification(user.getId(), null,
                    "Hồ sơ đã duyệt", "Tài khoản của bạn đã được nâng cấp thành Nhà tuyển dụng.", "SYSTEM", null);
        }

        return "Đã duyệt thành công công ty: " + company.getName();
    }

    @Transactional
    public String rejectCompany(Long companyId, String reason) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hồ sơ!"));

        company.setApprovalStatus("REJECTED");
        company.setRejectReason(reason);
        companyRepository.save(company);

        return "Đã từ chối công ty: " + company.getName();
    }

    @Transactional(readOnly = true)
    public Page<CompanyResponse> getAllApprovedCompanies(String searchName, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Company> companies;

        // Nếu người dùng có gõ vào ô tìm kiếm -> Gọi hàm tìm theo tên
        if (searchName != null && !searchName.trim().isEmpty()) {
            companies = companyRepository.findByNameContainingIgnoreCaseAndApprovalStatus(searchName.trim(), "APPROVED", pageable);
        }
        // Nếu ô tìm kiếm để trống -> Lấy tất cả công ty đã duyệt
        else {
            companies = companyRepository.findByApprovalStatus("APPROVED", pageable);
        }

        return companies.map(company -> {
            String shortDesc = company.getDescription();
            if (shortDesc != null && shortDesc.length() > 100) {
                shortDesc = shortDesc.substring(0, 100) + "...";
            }
            return CompanyResponse.builder()
                    .id(company.getId())
                    .name(company.getName())
                    .logoUrl(company.getLogoUrl())
                    .address(company.getAddress())
                    .website(company.getWebsite())
                    .shortDescription(shortDesc)
                    .build();
        });
    }

    @Transactional(readOnly = true)
    public CompanyDetailResponse getCompanyDetail(Long id) {

        // Dùng hàm vừa tạo ở Repo để quét đúng công ty APPROVED
        Company company = companyRepository.findByIdAndApprovalStatus(id, "APPROVED")
                .orElseThrow(() -> new RuntimeException("Công ty không tồn tại hoặc chưa được phê duyệt!"));

        return CompanyDetailResponse.builder()
                .id(company.getId())
                .name(company.getName())
                .logoUrl(company.getLogoUrl())
                .address(company.getAddress())
                .website(company.getWebsite())
                .description(company.getDescription())
                .build();
    }
}