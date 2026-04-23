package com.xuanthi.talentmatchingbe.service;

import com.xuanthi.talentmatchingbe.dto.company.AdminCompanyResponse;
import com.xuanthi.talentmatchingbe.dto.company.CompanyDetailResponse;
import com.xuanthi.talentmatchingbe.dto.company.CompanyResponse;
import com.xuanthi.talentmatchingbe.dto.company.CompanyUpdateRequest;
import com.xuanthi.talentmatchingbe.dto.user.EmployerUpgradeRequest;
import com.xuanthi.talentmatchingbe.entity.Company;
import com.xuanthi.talentmatchingbe.entity.CompanyFollow;
import com.xuanthi.talentmatchingbe.entity.User;
import com.xuanthi.talentmatchingbe.enums.Role;
import com.xuanthi.talentmatchingbe.mapper.CompanyMapper;
import com.xuanthi.talentmatchingbe.repository.CompanyFollowRepository;
import com.xuanthi.talentmatchingbe.repository.CompanyRepository;
import com.xuanthi.talentmatchingbe.repository.UserRepository;
import com.xuanthi.talentmatchingbe.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final CompanyMapper companyMapper;
    private final CloudinaryService cloudinaryService;
    private final CompanyFollowRepository companyFollowRepository;

    // ==========================================
    // 1. NGHIỆP VỤ CỦA ỨNG VIÊN (CANDIDATE)
    // ==========================================
    @Transactional
    public String requestUpgradeToEmployer(EmployerUpgradeRequest request) {
        User currentUser = SecurityUtils.getCurrentUser();
        
        if (currentUser == null) {
            log.warn("Unauthorized attempt to upgrade to employer");
            throw new RuntimeException("Vui lòng đăng nhập trước!");
        }

        log.info("User {} requesting employer upgrade with company: {}", currentUser.getEmail(), request.getCompanyName());

        // Kiểm tra xem đã gửi yêu cầu chưa
        Optional<Company> existingCompany = companyRepository.findByUserId(currentUser.getId());
        if (existingCompany.isPresent()) {
            String status = existingCompany.get().getApprovalStatus();
            if ("PENDING".equals(status)) {
                log.warn("User {} has pending company request", currentUser.getEmail());
                throw new RuntimeException("Hồ sơ công ty của bạn đang được xét duyệt!");
            } else if ("APPROVED".equals(status)) {
                log.info("User {} is already employer", currentUser.getEmail());
                throw new RuntimeException("Bạn đã là Nhà tuyển dụng rồi!");
            }
        }

        // Kiểm tra trùng Mã số thuế
        if (companyRepository.existsByTaxCode(request.getTaxCode()) &&
                (existingCompany.isEmpty() || !existingCompany.get().getTaxCode().equals(request.getTaxCode()))) {
            log.warn("Duplicate tax code: {} attempted by user {}", request.getTaxCode(), currentUser.getEmail());
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
        company.setRejectReason(null);

        companyRepository.save(company);
        log.info("Company application saved successfully for user: {}", currentUser.getEmail());
        return "Nộp hồ sơ thành công! Vui lòng chờ Admin xét duyệt.";
    }

    // ==========================================
    // 2. NGHIỆP VỤ CỦA QUẢN TRỊ VIÊN (ADMIN)
    // ==========================================
    @Transactional(readOnly = true)
    public List<AdminCompanyResponse> getPendingCompanies() {
        log.debug("Fetching pending company applications");
        List<Company> pendingCompanies = companyRepository.findByApprovalStatus("PENDING");
        log.info("Found {} pending company applications", pendingCompanies.size());

        return pendingCompanies.stream().map(company -> AdminCompanyResponse.builder()
                .id(company.getId())
                .name(company.getName())
                .taxCode(company.getTaxCode())
                .address(company.getAddress())
                .website(company.getWebsite())
                .businessLicenseUrl(company.getBusinessLicenseUrl())
                .hrPosition(company.getHrPosition())
                .approvalStatus(company.getApprovalStatus())
                .applicantEmail(company.getUser() != null ? company.getUser().getEmail() : null)
                .applicantName(company.getUser() != null ? company.getUser().getFullName() : null)
                .build()
        ).collect(Collectors.toList());
    }

    @Transactional
    public String approveCompany(Long companyId) {
        log.info("Admin approving company ID: {}", companyId);
        
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> {
                    log.error("Company not found for approval: {}", companyId);
                    return new RuntimeException("Không tìm thấy hồ sơ công ty!");
                });

        company.setApprovalStatus("APPROVED");
        companyRepository.save(company);

        // Nâng cấp Role cho User
        User user = company.getUser();
        if (user != null) {
            user.setRole(Role.EMPLOYER);
            userRepository.save(user);
            log.info("User {} role upgraded to EMPLOYER", user.getEmail());

            // (Tùy chọn) Gửi thông báo
            try {
                notificationService.sendNotification(user.getId(), null,
                        "Hồ sơ đã duyệt", "Tài khoản của bạn đã được nâng cấp thành Nhà tuyển dụng.", "SYSTEM", null);
                log.debug("Notification sent to user: {}", user.getEmail());
            } catch (Exception e) {
                log.warn("Failed to send notification to user {}: {}", user.getEmail(), e.getMessage());
            }
        }

        log.info("Company {} approved successfully", company.getName());
        return "Đã duyệt thành công công ty: " + company.getName();
    }

    @Transactional
    public String rejectCompany(Long companyId, String reason) {
        log.info("Admin rejecting company ID: {} with reason: {}", companyId, reason);
        
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> {
                    log.error("Company not found for rejection: {}", companyId);
                    return new RuntimeException("Không tìm thấy hồ sơ!");
                });

        company.setApprovalStatus("REJECTED");
        company.setRejectReason(reason);
        companyRepository.save(company);

        log.info("Company {} rejected", company.getName());
        return "Đã từ chối công ty: " + company.getName();
    }

    @Transactional(readOnly = true)
    public Page<CompanyResponse> getAllApprovedCompanies(String searchName, int page, int size) {
        log.debug("Fetching approved companies - search: {}, page: {}, size: {}", searchName, page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Company> companies;

        if (StringUtils.hasText(searchName)) {
            log.debug("Searching companies by name: {}", searchName);
            companies = companyRepository.findByNameContainingIgnoreCaseAndApprovalStatus(searchName.trim(), "APPROVED", pageable);
        } else {
            companies = companyRepository.findByApprovalStatus("APPROVED", pageable);
        }

        log.info("Found {} approved companies (page {} of {})", companies.getNumberOfElements(), page, companies.getTotalPages());

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
        log.debug("Fetching company detail for ID: {}", id);
        
        Company company = companyRepository.findByIdAndApprovalStatus(id, "APPROVED")
                .orElseThrow(() -> {
                    log.warn("Company not found or not approved: {}", id);
                    return new RuntimeException("Công ty không tồn tại hoặc chưa được phê duyệt!");
                });

        log.info("Company detail retrieved: {}", company.getName());

        return CompanyDetailResponse.builder()
                .id(company.getId())
                .name(company.getName())
                .logoUrl(company.getLogoUrl())
                .address(company.getAddress())
                .website(company.getWebsite())
                .description(company.getDescription())
                .employerEmail(company.getUser() != null ? company.getUser().getEmail() : null)
                .build();
    }

    @Transactional
    public CompanyResponse updateCompanyByHr(Long hrUserId, CompanyUpdateRequest request) {

        // 1. Tìm hoặc tạo mới (KHÔNG BƠM MÃ GIẢ NỮA)
        Company company = companyRepository.findByUserId(hrUserId)
                .orElseGet(() -> {
                    Company newCompany = new Company();
                    newCompany.setUser(userRepository.getReferenceById(hrUserId));
                    newCompany.setLocked(false);
                    return newCompany;
                });

        // 2. Kiểm tra trùng Tên công ty
        if (companyRepository.existsByName(request.getName()) && !request.getName().equals(company.getName())) {
            throw new IllegalArgumentException("Tên công ty này đã có người đăng ký!");
        }

        // 3. KIỂM TRA TRÙNG MÃ SỐ THUẾ (Bảo mật cốt lõi)
        if (companyRepository.existsByTaxCode(request.getTaxCode()) && !request.getTaxCode().equals(company.getTaxCode())) {
            throw new IllegalArgumentException("Mã số thuế này đã được đăng ký cho một tài khoản khác!");
        }

        // 4. Nếu bị khóa thì cấm sửa
        if (company.isLocked()) {
            throw new AccessDeniedException("Công ty của bạn đang bị Admin khóa, không thể chỉnh sửa!");
        }

        // 5. Cập nhật dữ liệu thật từ Frontend gửi lên
        company.setName(request.getName());
        company.setAddress(request.getAddress());
        company.setWebsite(request.getWebsite());
        company.setDescription(request.getDescription());

        // NHẬN MÃ SỐ THUẾ THẬT Ở ĐÂY
        company.setTaxCode(request.getTaxCode());

        // 6. Lưu xuống DB
        Company savedCompany = companyRepository.save(company);

        return companyMapper.toCompanyResponse(savedCompany);
    }

    /**
     * Dành cho Admin Khóa / Mở khóa công ty
     */
    @Transactional
    public String toggleLockCompany(Long companyId) {
        // 1. Tìm công ty theo ID
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy công ty với ID: " + companyId));

        // 2. Đảo ngược trạng thái khóa
        boolean newStatus = !company.isLocked();
        company.setLocked(newStatus);

        // 3. Lưu xuống Database
        companyRepository.save(company);

        // 4. Ghi log
        log.info("Admin đã {} công ty ID: {}", newStatus ? "KHÓA" : "MỞ KHÓA", companyId);

        // 5. Trả về thông báo động
        return newStatus ? "Đã KHÓA công ty thành công!" : "Đã MỞ KHÓA công ty thành công!";
    }

    @Transactional
    public String uploadLogo(Long hrUserId, MultipartFile file) {
        // 1. Fail-fast: Kiểm tra file rỗng
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File logo không được để trống!");
        }

        // 2. Kiểm tra định dạng (Chỉ cho phép ảnh, chống hacker upload script bậy bạ)
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File tải lên phải là hình ảnh (JPG, PNG...)!");
        }

        // 3. Tìm công ty của HR hiện tại
        Company company = companyRepository.findByUserId(hrUserId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy công ty của bạn!"));

        // 4. Kiểm tra an ninh: Mõm có đang bị khóa không?
        if (company.isLocked()) {
            throw new AccessDeniedException("Công ty của bạn đang bị Admin khóa, không thể đổi logo!");
        }

        try {
            // ✅ GỌI HÀM CỦA SẾP VÀ TRUYỀN THÊM TÊN THƯ MỤC: "company_logos"
            String logoUrl = cloudinaryService.uploadFile(file, "company_logos");

            // Cập nhật link ảnh vào Entity và lưu DB
            company.setLogoUrl(logoUrl);
            companyRepository.save(company);

            log.info("HR ID {} đã cập nhật logo mới cho công ty ID {}", hrUserId, company.getId());

            return logoUrl;

        } catch (IOException e) {
            // Bắt chính xác cái lỗi IOException mà hàm của sếp ném ra
            log.error("Lỗi đọc ghi file I/O khi upload logo cho HR ID: {}", hrUserId, e);
            throw new RuntimeException("Không thể đọc được file tải lên, vui lòng thử lại!");
        } catch (Exception e) {
            log.error("Lỗi Cloudinary khi upload logo cho HR ID: {}", hrUserId, e);
            throw new RuntimeException("Máy chủ lưu trữ ảnh đang bận, vui lòng thử lại sau!");
        }
}
    /**
     * Logic Bật/Tắt theo dõi công ty (Dùng cho Ứng viên)
     */
    @Transactional
    public String toggleFollow(Long candidateId, Long companyId) {
        // 1. Kiểm tra công ty có tồn tại không
        if (!companyRepository.existsById(companyId)) {
            throw new IllegalArgumentException("Không tìm thấy công ty này trên hệ thống!");
        }

        // 2. Tìm xem đã follow chưa
        Optional<CompanyFollow> existingFollow = companyFollowRepository.findByUserIdAndCompanyId(candidateId, companyId);

        if (existingFollow.isPresent()) {
            // Bấm lần 2 -> Hủy theo dõi
            companyFollowRepository.delete(existingFollow.get());
            log.info("Candidate ID {} đã UNFOLLOW công ty ID {}", candidateId, companyId);
            return "Đã bỏ theo dõi công ty!";
        } else {
            // Bấm lần 1 -> Lưu theo dõi
            // Tối ưu tốc độ: Dùng getReferenceById để lấy proxy thay vì chọc xuống DB tìm lại User và Company
            CompanyFollow newFollow = CompanyFollow.builder()
                    .user(userRepository.getReferenceById(candidateId))
                    .company(companyRepository.getReferenceById(companyId))
                    .build();

            companyFollowRepository.save(newFollow);
            log.info("Candidate ID {} đã FOLLOW công ty ID {}", candidateId, companyId);
            return "Đã theo dõi công ty thành công!";
        }
    }

    /**
     * Lấy danh sách công ty đang theo dõi (Phân trang)
     */
    @Transactional(readOnly = true)
    public Page<CompanyResponse> getFollowingCompanies(Long candidateId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        // Gọi thẳng hàm Query tối ưu ở Repository
        Page<Company> followedCompanies = companyFollowRepository.findFollowedCompaniesByUserId(candidateId, pageable);

        // Dùng cái companyMapper anh em mình làm ban nãy để ép sang DTO gửi cho FE
        return followedCompanies.map(companyMapper::toCompanyResponse);
    }
    }

