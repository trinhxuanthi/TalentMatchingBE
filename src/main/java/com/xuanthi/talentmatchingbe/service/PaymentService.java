package com.xuanthi.talentmatchingbe.service;

import com.xuanthi.talentmatchingbe.dto.payment.PricingPlanResponse;
import com.xuanthi.talentmatchingbe.entity.Payment;
import com.xuanthi.talentmatchingbe.entity.PricingPlan;
import com.xuanthi.talentmatchingbe.entity.User;
import com.xuanthi.talentmatchingbe.enums.AccountType;
import com.xuanthi.talentmatchingbe.repository.PaymentRepository;
import com.xuanthi.talentmatchingbe.repository.PricingPlanRepository;
import com.xuanthi.talentmatchingbe.repository.UserRepository;
import com.xuanthi.talentmatchingbe.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PricingPlanRepository planRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final MailService mailService;
    private final PaymentRepository paymentRepository;

    // ==========================================
    // 1. LẤY DANH SÁCH GÓI CƯỚC (Dựa theo Role)
    // ==========================================
    public List<PricingPlanResponse> getAvailablePlans() {
        User currentUser = SecurityUtils.getRequiredCurrentUser();
        String currentRole = currentUser.getRole().name(); // 'EMPLOYER' hoặc 'CANDIDATE'

        List<PricingPlan> plans = planRepository.findAllByTargetRoleAndIsActiveTrue(currentRole);

        return plans.stream().map(plan -> PricingPlanResponse.builder()
                .id(plan.getId())
                .name(plan.getName())
                .description(plan.getDescription())
                .durationDays(plan.getDurationDays())
                .basePrice(plan.getBasePrice())
                .discountPercent(plan.getDiscountPercent())
                .finalPrice(plan.getFinalPrice())
                .build()).collect(Collectors.toList());
    }

    // ==========================================
    // 2. TẠO LINK THANH TOÁN VNPAY
    // ==========================================
    public String createVNPayUrl(Long planId) {
        User currentUser = SecurityUtils.getRequiredCurrentUser();

        PricingPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy gói cước này!"));

        // 🛑 BẢO MẬT: Chặn ứng viên mua gói HR và ngược lại
        if (!plan.getTargetRole().equals(currentUser.getRole().name())) {
            throw new AccessDeniedException("Bạn không có quyền mua gói cước của đối tượng khác!");
        }

        // TẠM THỜI: Em giả lập URL trả về. Sau này sếp config code tạo Hash VNPay vào đây.
        // Mã giao dịch lưu kèm ID User và ID gói cước để xíu nữa xử lý callback
        String txnRef = currentUser.getId() + "_" + plan.getId() + "_" + System.currentTimeMillis();
        long amountToPay = plan.getFinalPrice().longValue();

        log.info("Tạo link thanh toán gói {} cho User ID {} số tiền {}", plan.getName(), currentUser.getId(), amountToPay);

        // Trả về link VNPay ảo (Sếp sẽ thay bằng logic build params thật của VNPay)
        return "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?vnp_TxnRef=" + txnRef + "&vnp_Amount=" + (amountToPay * 100);
    }

    // ==========================================
    // 3. XỬ LÝ KHI VNPAY TRẢ VỀ THÀNH CÔNG (CỘNG NGÀY PRO)
    // ==========================================
    @Transactional // ✅ Tiền bạc là phải có Transactional bảo vệ sếp nhé
    public void processPaymentSuccess(String txnRef, String bankCode) {
        // Cấu trúc txnRef: {userId}_{planId}_{timestamp}
        String[] parts = txnRef.split("_");
        Long userId = Long.parseLong(parts[0]);
        Long planId = Long.parseLong(parts[1]);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy User thanh toán!"));
        PricingPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy Gói cước!"));

        // Nâng cấp lên PRO
        user.setAccountType(AccountType.PRO);

        // Tính toán ngày hết hạn (Nếu đang còn hạn thì cộng dồn, nếu hết hạn thì tính từ hôm nay)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime currentExpiry = (user.getProExpiredAt() != null && user.getProExpiredAt().isAfter(now))
                ? user.getProExpiredAt()
                : now;

        user.setProExpiredAt(currentExpiry.plusDays(plan.getDurationDays()));

        userRepository.save(user);
        // 2. 🚀 LƯU HÓA ĐƠN VÀO BẢNG PAYMENT (Mới thêm)
        Payment payment = Payment.builder()
                .user(user)
                .plan(plan)
                .amount(plan.getFinalPrice().doubleValue()) // Lấy giá cuối cùng từ hàm sếp vừa đưa
                .txnRef(txnRef)
                .status("SUCCESS")
                .bankCode(bankCode)
                .build();
        paymentRepository.save(payment);
        log.info("✅ TÀI KHOẢN {} ĐÃ LÊN PRO. Hạn dùng đến: {}", user.getEmail(), user.getProExpiredAt());

        // 🚀 BÓP CÒ BẮN THÔNG BÁO "TINH TING" VIP PRO
        String planName = plan.getName() != null ? plan.getName() : "Gói Premium";

        notificationService.sendNotification(
                user.getId(),           // Người nhận: Chính là người vừa thanh toán
                null,                   // Người gửi: null -> Hệ thống tự động gửi
                "Nâng cấp PRO thành công! 💎",
                "Tuyệt vời! Tài khoản của bạn đã được nâng cấp lên [" + planName + "]. Khám phá các đặc quyền ngay bây giờ!",
                "PAYMENT_SUCCESS",      // Phân loại: Thành công
                plan.getId()            // ID gói cước (nếu frontend cần)
        );

        mailService.sendProUpgradeEmail(
                user.getEmail(),
                user.getFullName(),
                plan.getName(),
                user.getProExpiredAt()
        );
    }
}