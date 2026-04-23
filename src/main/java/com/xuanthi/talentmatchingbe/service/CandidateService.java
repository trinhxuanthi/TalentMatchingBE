package com.xuanthi.talentmatchingbe.service;

import com.xuanthi.talentmatchingbe.dto.user.ProfileViewResponse;
import com.xuanthi.talentmatchingbe.dto.user.UserResponse;
import com.xuanthi.talentmatchingbe.entity.ProfileView;
import com.xuanthi.talentmatchingbe.entity.User;
import com.xuanthi.talentmatchingbe.enums.ProFeature;
import com.xuanthi.talentmatchingbe.mapper.ProfileViewMapper;
import com.xuanthi.talentmatchingbe.mapper.UserMapper;
import com.xuanthi.talentmatchingbe.repository.ProfileViewRepository;
import com.xuanthi.talentmatchingbe.repository.UserRepository;
import com.xuanthi.talentmatchingbe.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CandidateService {

    private final UserRepository userRepository;
    private final ProfileViewRepository profileViewRepository;
    private final ProService proService;
    private final NotificationService notificationService;

    // Hai ông thần Mapper giúp chuyển đổi Entity -> DTO
    private final UserMapper userMapper;
    private final ProfileViewMapper profileViewMapper;

    // ==========================================
    // 1. HR XEM CHI TIẾT ỨNG VIÊN (TỰ ĐỘNG GHI LOG)
    // ==========================================
    @Transactional
    public UserResponse getCandidateDetail(Long candidateId) {
        User currentHr = SecurityUtils.getRequiredCurrentUser();

        // 1. Kiểm tra xem ứng viên có tồn tại không
        User candidate = userRepository.findById(candidateId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy ứng viên!"));

        // 2. LOGIC TRẠM THU PHÍ (CHẶN BASIC - THẢ PRO)
        if (!currentHr.isPro()) {
            // Đếm số ứng viên khác nhau đã xem
            long viewCount = profileViewRepository.countUniqueViewsByEmployerId(currentHr.getId());

            // Nếu đã xem 5 người, và người đang định xem là người MỚI (chưa xem bao giờ) -> CHẶN
            boolean alreadyViewed = profileViewRepository.existsByEmployerIdAndCandidateId(currentHr.getId(), candidateId);

            if (viewCount >= 5 && !alreadyViewed) {
                throw new AccessDeniedException("Gói BASIC chỉ được xem chi tiết tối đa 5 ứng viên. Vui lòng nâng cấp PRO để xem không giới hạn!");
            }
        } else {
            // 3. NẾU LÀ PRO: Nếu họ xem đến người thứ 6 trở đi, ghi Log đặc quyền PRO
            long viewCount = profileViewRepository.countUniqueViewsByEmployerId(currentHr.getId());
            if (viewCount >= 5) {
                proService.validateAndLogUsage(currentHr, ProFeature.WHO_VIEWED_PROFILE);
            }
        }

        // 4. Ghi lại vết xem hồ sơ và BẮN THÔNG BÁO (Chỉ thực hiện ở lần xem đầu tiên)
        if (!profileViewRepository.existsByEmployerIdAndCandidateId(currentHr.getId(), candidateId)) {
            // 4.1 Lưu Database
            ProfileView view = ProfileView.builder()
                    .candidate(candidate)
                    .employer(currentHr)
                    .build();
            profileViewRepository.save(view);

            // 4.2 Lấy thông tin công ty an toàn (Chống NullPointerException)
            String companyName = (currentHr.getCompany() != null) ? currentHr.getCompany().getName() : "Một nhà tuyển dụng ẩn danh";
            Long companyId = (currentHr.getCompany() != null) ? currentHr.getCompany().getId() : null;

            // 4.3 🚀 BÓP CÒ BẮN THÔNG BÁO REAL-TIME
            notificationService.sendNotification(
                    candidateId,          // Người nhận: Ứng viên
                    currentHr.getId(),    // Người gửi: HR
                    "Hồ sơ của bạn có lượt xem mới! 🔥",
                    companyName + " vừa xem hồ sơ cá nhân của bạn.",
                    "PROFILE_VIEW",       // Loại thông báo
                    companyId             // Trỏ link về công ty
            );
        }

        return userMapper.toUserResponse(candidate);
    }


    // ==========================================
    // 3. ỨNG VIÊN PRO XEM AI ĐÃ VÀO HỒ SƠ
    // ==========================================
    public Page<ProfileViewResponse> getWhoViewedMyProfile(Pageable pageable) {
        User currentCandidate = SecurityUtils.getRequiredCurrentUser();

        // 🛑 ÔNG QUẢN GIA GÁC CỔNG: Chặn tài khoản Basic bắt nâng cấp
        proService.validateAndLogUsage(currentCandidate, ProFeature.WHO_VIEWED_PROFILE);

        // Lấy danh sách lịch sử
        Page<ProfileView> views = profileViewRepository.findByCandidateIdOrderByViewedAtDesc(currentCandidate.getId(), pageable);

        // ✅ Chuyển Page<Entity> thành Page<DTO> chứa tên Công ty + Tên HR
        return views.map(profileViewMapper::toResponse);
    }
}