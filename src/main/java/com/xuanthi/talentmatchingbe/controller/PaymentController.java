package com.xuanthi.talentmatchingbe.controller;

import com.xuanthi.talentmatchingbe.dto.payment.PricingPlanResponse;
import com.xuanthi.talentmatchingbe.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payment API", description = "Quản lý gói cước và thanh toán VNPay")
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/plans")
    @Operation(summary = "Lấy danh sách các gói cước (Tự động lọc theo Role của người dùng)")
    public ResponseEntity<List<PricingPlanResponse>> getAvailablePlans() {
        return ResponseEntity.ok(paymentService.getAvailablePlans());
    }

    @PostMapping("/create-url/{planId}")
    @Operation(summary = "Tạo URL chuyển hướng sang cổng thanh toán VNPay")
    public ResponseEntity<Map<String, String>> createPaymentUrl(@PathVariable Long planId) {
        String url = paymentService.createVNPayUrl(planId);
        return ResponseEntity.ok(Map.of("paymentUrl", url));
    }

    // Frontend sẽ trỏ ReturnURL của VNPay về API này (Hoặc Frontend nhận rồi gọi API này)
    @GetMapping("/vnpay-callback")
    @Operation(summary = "Xử lý kết quả từ VNPay và nâng cấp tài khoản")
    public ResponseEntity<Void> vnpayCallback(
            @RequestParam("vnp_ResponseCode") String responseCode,
            @RequestParam("vnp_TxnRef") String txnRef,
            @RequestParam(value = "vnp_BankCode", required = false) String bankCode) {

        // 1. Cấu hình URL của Frontend (Sếp đổi thành port/địa chỉ thực tế của sếp nhé)
        String frontendUrl = "http://localhost:3000/payment-result";
        String finalUrl;

        try {
            // 2. Kiểm tra mã phản hồi từ VNPay (00 là thành công)
            if ("00".equals(responseCode)) {
                // Xử lý nghiệp vụ nâng cấp PRO và lưu hóa đơn
                paymentService.processPaymentSuccess(txnRef, bankCode);

                // Gắn thêm params vào URL để Frontend hiển thị thông báo thành công
                finalUrl = frontendUrl + "?status=success&txnRef=" + txnRef;
            } else {
                log.warn("VNPay Payment Failed. Code: {}, Ref: {}", responseCode, txnRef);
                // Gắn params báo lỗi
                finalUrl = frontendUrl + "?status=failed&errorCode=" + responseCode;
            }
        } catch (Exception e) {
            log.error("Error processing VNPay callback: {}", e.getMessage());
            finalUrl = frontendUrl + "?status=error&message=system_error";
        }

        // 3. 🚀 THỰC HIỆN ĐIỀU HƯỚNG (REDIRECT)
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(java.net.URI.create(finalUrl))
                .build();
    }
}