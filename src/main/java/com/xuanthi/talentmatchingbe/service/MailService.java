package com.xuanthi.talentmatchingbe.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {
    private final JavaMailSender mailSender;

    public void sendOtpEmail(String toEmail, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("Talent Matching Support <" + "your-email@gmail.com" + ">");
            helper.setTo(toEmail);
            helper.setSubject("[Talent Matching] Mã xác thực (OTP) đặt lại mật khẩu của bạn");

            // Giao diện HTML chuyên nghiệp
            String htmlContent = " <div style='font-family: Arial, Helvetica, sans-serif; max-width: 600px; margin: 0 auto; border: 1px solid #e0e0e0; border-radius: 8px; overflow: hidden;'> " +
                    "   <div style='background-color: #007bff; padding: 20px; text-align: center;'> " +
                    "       <h1 style='color: #ffffff; margin: 0; font-size: 24px;'>Talent Matching</h1> " +
                    "   </div> " +
                    "   <div style='padding: 30px; color: #333333; line-height: 1.6;'> " +
                    "       <p style='font-size: 16px;'>Xin chào,</p> " +
                    "       <p>Chúng tôi đã nhận được yêu cầu đặt lại mật khẩu cho tài khoản liên kết với địa chỉ email này. Để tiếp tục, vui lòng sử dụng mã xác thực (OTP) dưới đây:</p> " +
                    "       <div style='background-color: #f4f7f9; border-radius: 4px; padding: 20px; text-align: center; margin: 25px 0;'> " +
                    "           <span style='display: block; font-size: 14px; color: #777; margin-bottom: 10px; font-weight: bold;'>MÃ XÁC THỰC CỦA BẠN</span> " +
                    "           <span style='font-size: 32px; font-weight: bold; letter-spacing: 8px; color: #007bff;'>" + otp + "</span> " +
                    "       </div> " +
                    "       <p style='font-size: 14px; color: #555;'>Mã này có hiệu lực trong vòng <b>5 phút</b> kể từ thời điểm email này được gửi đi. Sau thời gian này, bạn sẽ cần thực hiện yêu cầu mới.</p> " +
                    "       <div style='background-color: #fff4f4; border-left: 4px solid #dc3545; padding: 15px; margin-top: 20px;'> " +
                    "           <p style='margin: 0; font-size: 13px; color: #856404;'><b>Lưu ý bảo mật:</b> Nhân viên của chúng tôi sẽ không bao giờ yêu cầu bạn cung cấp mã này. Vui lòng không chia sẻ mã này với bất kỳ ai để đảm bảo an toàn cho tài khoản cá nhân.</p> " +
                    "       </div> " +
                    "       <p style='margin-top: 25px;'>Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email này hoặc liên hệ với bộ phận hỗ trợ của chúng tôi nếu bạn lo ngại về an toàn tài khoản.</p> " +
                    "       <p>Trân trọng,<br><b>Đội ngũ kỹ thuật Talent Matching</b></p> " +
                    "   </div> " +
                    "   <div style='background-color: #f8f9fa; padding: 15px; text-align: center; font-size: 12px; color: #999;'> " +
                    "       © 2026 Talent Matching Project. All rights reserved.<br> " +
                    "       Email này được gửi tự động từ hệ thống, vui lòng không phản hồi lại địa chỉ này. " +
                    "   </div> " +
                    " </div> ";

            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Lỗi khi gửi email xác thực: " + e.getMessage());
        }
    }
}