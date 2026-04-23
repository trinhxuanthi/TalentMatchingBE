package com.xuanthi.talentmatchingbe.service;

import com.xuanthi.talentmatchingbe.entity.User;
import com.xuanthi.talentmatchingbe.enums.AccountType;
import com.xuanthi.talentmatchingbe.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProExpirationService {

    private final UserRepository userRepository;

    // Chạy vào lúc 00:00:00 (nửa đêm) mỗi ngày
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void downgradeExpiredProUsers() {
        log.info("Bắt đầu quét và giáng cấp các tài khoản PRO đã hết hạn...");

        LocalDateTime now = LocalDateTime.now();

        // Sếp nhớ thêm hàm findByAccountTypeAndProExpiredAtBefore vào UserRepository nhé
        List<User> expiredUsers = userRepository.findByAccountTypeAndProExpiredAtBefore(AccountType.PRO, now);

        if (expiredUsers.isEmpty()) {
            log.info("Hôm nay không có tài khoản nào hết hạn.");
            return;
        }

        for (User user : expiredUsers) {
            user.setAccountType(AccountType.BASIC);
            log.info("Đã giáng cấp User ID: {} - Email: {}", user.getId(), user.getEmail());

            // 💡 TUYỆT CHIÊU THÊM: Nếu sếp muốn bài Job của HR này mất luôn tích xanh VIP khi họ hết hạn PRO thì chọc vào JobRepository update luôn ở đây.
        }

        userRepository.saveAll(expiredUsers);
        log.info("Hoàn tất giáng cấp {} tài khoản.", expiredUsers.size());
    }
}