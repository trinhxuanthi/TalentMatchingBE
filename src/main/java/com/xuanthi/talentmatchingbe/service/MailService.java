package com.xuanthi.talentmatchingbe.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {
    private final JavaMailSender mailSender;

    @Value("${mail.from.address:support@talentmatching.com}")
    private String fromAddress;

    @Value("${mail.from.name:Talent Matching Support}")
    private String fromName;

    // =========================================================================
    // HÀM GỬI EMAIL CHUNG (Dùng nội bộ để tránh lặp code)
    // =========================================================================
    private void sendEmailInternal(String toEmail, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromAddress, fromName);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("✅ Đã gửi email thành công tới: {}", toEmail);

        } catch (MessagingException e) {
            log.error("❌ Lỗi MessagingException khi gửi email tới {}: {}", toEmail, e.getMessage());
        } catch (Exception e) {
            log.error("❌ Lỗi không mong muốn khi gửi email tới {}: {}", toEmail, e.getMessage());
        }
    }

    // =========================================================================
    // 1. EMAIL GỬI OTP (Cũ của sếp - Đã gắn @Async)
    // =========================================================================
    @Async
    public void sendOtpEmail(String toEmail, String otp) {
        if (!StringUtils.hasText(toEmail) || !StringUtils.hasText(otp)) return;
        log.info("Preparing to send OTP email to: {}", toEmail);

        String subject = "[Talent Matching] Mã xác thực (OTP) đặt lại mật khẩu của bạn";
        String htmlContent = buildOtpEmailHtml(otp);
        sendEmailInternal(toEmail, subject, htmlContent);
    }

    // =========================================================================
    // 2. EMAIL CẬP NHẬT TRẠNG THÁI ỨNG TUYỂN (Mới thêm)
    // =========================================================================
    @Async
    public void sendApplicationStatusEmail(String toEmail, String candidateName, String jobTitle, String companyName, String status, String notes) {
        if (!StringUtils.hasText(toEmail)) return;

        String subject = "[Talent Matching] Cập nhật kết quả ứng tuyển: " + jobTitle;
        String htmlContent = buildStatusEmailHtml(candidateName, jobTitle, companyName, status, notes);
        sendEmailInternal(toEmail, subject, htmlContent);
    }

    // =========================================================================
    // 3. EMAIL THÔNG BÁO NÂNG CẤP PRO THÀNH CÔNG (Mới thêm)
    // =========================================================================
    @Async
    public void sendProUpgradeEmail(String toEmail, String userName, String planName, LocalDateTime expiredAt) {
        if (!StringUtils.hasText(toEmail)) return;

        String subject = "[Talent Matching] Nâng cấp tài khoản PRO thành công! 💎";
        String expiryStr = expiredAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        String htmlContent = buildProUpgradeEmailHtml(userName, planName, expiryStr);
        sendEmailInternal(toEmail, subject, htmlContent);
    }

    // =========================================================================
    // CÁC HÀM XÂY DỰNG GIAO DIỆN HTML (HTML BUILDERS)
    // =========================================================================

    private String buildOtpEmailHtml(String otp) {
        // ĐÃ PHỤC HỒI NGUYÊN VẸN HTML CỦA SẾP
        return "<div style='font-family: Arial, Helvetica, sans-serif; max-width: 600px; margin: 0 auto; border: 1px solid #e0e0e0; border-radius: 8px; overflow: hidden;'>" +
                "   <div style='background-color: #007bff; padding: 20px; text-align: center;'>" +
                "       <h1 style='color: #ffffff; margin: 0; font-size: 24px;'>Talent Matching</h1>" +
                "   </div>" +
                "   <div style='padding: 30px; color: #333333; line-height: 1.6;'>" +
                "       <p style='font-size: 16px;'>Xin chào,</p>" +
                "       <p>Chúng tôi đã nhận được yêu cầu đặt lại mật khẩu cho tài khoản liên kết với địa chỉ email này. Để tiếp tục, vui lòng sử dụng mã xác thực (OTP) dưới đây:</p>" +
                "       <div style='background-color: #f4f7f9; border-radius: 4px; padding: 20px; text-align: center; margin: 25px 0;'>" +
                "           <span style='display: block; font-size: 14px; color: #777; margin-bottom: 10px; font-weight: bold;'>MÃ XÁC THỰC CỦA BẠN</span>" +
                "           <span style='font-size: 32px; font-weight: bold; letter-spacing: 8px; color: #007bff;'>" + escapeHtml(otp) + "</span>" +
                "       </div>" +
                "       <p style='font-size: 14px; color: #555;'>Mã này có hiệu lực trong vòng <b>5 phút</b> kể từ thời điểm email này được gửi đi. Sau thời gian này, bạn sẽ cần thực hiện yêu cầu mới.</p>" +
                "       <div style='background-color: #fff4f4; border-left: 4px solid #dc3545; padding: 15px; margin-top: 20px;'>" +
                "           <p style='margin: 0; font-size: 13px; color: #856404;'><b>Lưu ý bảo mật:</b> Nhân viên của chúng tôi sẽ không bao giờ yêu cầu bạn cung cấp mã này. Vui lòng không chia sẻ mã này với bất kỳ ai để đảm bảo an toàn cho tài khoản cá nhân.</p>" +
                "       </div>" +
                "       <p style='margin-top: 25px;'>Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email này hoặc liên hệ với bộ phận hỗ trợ của chúng tôi nếu bạn lo ngại về an toàn tài khoản.</p>" +
                "       <p>Trân trọng,<br><b>Đội ngũ kỹ thuật Talent Matching</b></p>" +
                "   </div>" +
                "   <div style='background-color: #f8f9fa; padding: 15px; text-align: center; font-size: 12px; color: #999;'>" +
                "       © 2026 Talent Matching Project. All rights reserved.<br>" +
                "       Email này được gửi tự động từ hệ thống, vui lòng không phản hồi lại địa chỉ này." +
                "   </div>" +
                "</div>";
    }

    private String buildStatusEmailHtml(String name, String jobTitle, String company, String status, String notes) {
        String headerColor = "#007bff";
        if (status.contains("phỏng vấn")) headerColor = "#fd7e14";
        else if (status.contains("chấp nhận")) headerColor = "#28a745";
        else if (status.contains("từ chối")) headerColor = "#dc3545";

        String noteSection = (notes != null && !notes.trim().isEmpty())
                ? "<div style='background-color: #f8f9fa; border-left: 4px solid " + headerColor + "; padding: 15px; margin-top: 20px;'>" +
                "<p style='margin: 0; font-size: 14px;'><b>Ghi chú từ HR:</b><br/>" + escapeHtml(notes) + "</p></div>"
                : "";

        return "<div style='font-family: Arial, Helvetica, sans-serif; max-width: 600px; margin: 0 auto; border: 1px solid #e0e0e0; border-radius: 8px; overflow: hidden;'>" +
                "   <div style='background-color: " + headerColor + "; padding: 20px; text-align: center;'>" +
                "       <h1 style='color: #ffffff; margin: 0; font-size: 24px;'>Cập Nhật Ứng Tuyển</h1>" +
                "   </div>" +
                "   <div style='padding: 30px; color: #333333; line-height: 1.6;'>" +
                "       <p style='font-size: 16px;'>Xin chào <b>" + escapeHtml(name) + "</b>,</p>" +
                "       <p>Hồ sơ ứng tuyển của bạn cho vị trí <b>" + escapeHtml(jobTitle) + "</b> tại công ty <b>" + escapeHtml(company) + "</b> vừa có cập nhật mới.</p>" +
                "       <div style='text-align: center; margin: 25px 0;'>" +
                "           <span style='display: inline-block; padding: 10px 20px; font-size: 18px; font-weight: bold; color: " + headerColor + "; border: 2px solid " + headerColor + "; border-radius: 25px;'>" + escapeHtml(status).toUpperCase() + "</span>" +
                "       </div>" +
                noteSection +
                "       <p style='margin-top: 25px;'>Vui lòng đăng nhập vào hệ thống Talent Matching để xem chi tiết và phản hồi (nếu cần).</p>" +
                "       <p>Trân trọng,<br><b>Talent Matching Support</b></p>" +
                "   </div>" +
                "</div>";
    }

    private String buildProUpgradeEmailHtml(String name, String planName, String expiry) {
        return "<div style='font-family: Arial, Helvetica, sans-serif; max-width: 600px; margin: 0 auto; border: 1px solid #e0e0e0; border-radius: 8px; overflow: hidden;'>" +
                "   <div style='background-color: #6f42c1; padding: 20px; text-align: center;'>" +
                "       <h1 style='color: #ffffff; margin: 0; font-size: 24px;'>💎 Nâng Cấp Thành Công 💎</h1>" +
                "   </div>" +
                "   <div style='padding: 30px; color: #333333; line-height: 1.6;'>" +
                "       <p style='font-size: 16px;'>Xin chào <b>" + escapeHtml(name) + "</b>,</p>" +
                "       <p>Cảm ơn bạn đã tin tưởng. Tài khoản của bạn đã được nâng cấp thành công lên gói <b>" + escapeHtml(planName) + "</b>.</p>" +
                "       <ul style='background-color: #f4f7f9; padding: 20px 40px; border-radius: 4px;'>" +
                "           <li>Gói cước: <b>" + escapeHtml(planName) + "</b></li>" +
                "           <li>Có hiệu lực đến: <b>" + escapeHtml(expiry) + "</b></li>" +
                "           <li>Trạng thái thanh toán: <span style='color: #28a745; font-weight: bold;'>HOÀN TẤT</span></li>" +
                "       </ul>" +
                "       <p style='margin-top: 25px;'>Bây giờ bạn đã có thể sử dụng các tính năng VIP như: Xem ai đã vào hồ sơ, Lọc AI không giới hạn,...</p>" +
                "       <p>Trân trọng,<br><b>Talent Matching Support</b></p>" +
                "   </div>" +
                "</div>";
    }

    private String escapeHtml(String text) {
        if (!StringUtils.hasText(text)) return "";
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }
}