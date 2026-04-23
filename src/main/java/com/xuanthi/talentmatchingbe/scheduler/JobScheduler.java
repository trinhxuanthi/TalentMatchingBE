package com.xuanthi.talentmatchingbe.scheduler;

import com.xuanthi.talentmatchingbe.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component // Biến class này thành một Bean để Spring quản lý
@RequiredArgsConstructor
@Slf4j
public class JobScheduler {

    private final JobRepository jobRepository;

    /**
     * Chạy tự động vào lúc 00:01 (1 phút qua nửa đêm) mỗi ngày
     * Giải thích Cron: Giây(0) Phút(1) Giờ(0) Ngày(*) Tháng(*) Thứ(?)
     */
    @Scheduled(cron = "0 1 0 * * ?")
    @Transactional
    public void scanAndCloseExpiredJobs() {
        log.info("⏳ [CRON JOB] Bắt đầu quét các bài tuyển dụng (Job) quá hạn...");

        try {
            // Lấy ngày hôm nay
            LocalDate today = LocalDate.now();

            // Chạy lệnh update hàng loạt
            int closedCount = jobRepository.closeExpiredJobs(today);

            log.info("✅ [CRON JOB] Hoàn tất! Đã tự động đóng [{}] bài tuyển dụng hết hạn.", closedCount);
        } catch (Exception e) {
            log.error("❌ [CRON JOB] Lỗi khi quét khóa Job: {}", e.getMessage());
        }
    }
}