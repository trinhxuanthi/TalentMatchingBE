package com.xuanthi.talentmatchingbe.service;

import com.xuanthi.talentmatchingbe.entity.AiSetting;
import com.xuanthi.talentmatchingbe.repository.AiSettingRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiSettingService {

    private final AiSettingRepository repository;
    private AiSetting currentSettings; // Bộ nhớ đệm RAM

    @PostConstruct
    public void init() {
        refreshCache();
    }

    public void refreshCache() {
        this.currentSettings = repository.findById(1L).orElseGet(() -> {
            log.info("[AI CONFIG] Khởi tạo cấu hình Admin mặc định...");
            AiSetting defaultSetting = AiSetting.builder()
                    .id(1L)
                    // BỘ LỌC CỨNG Ở JAVA
                    .filterTitleScore(65.0)
                    .filterTotalScore(50.0)
                    // BỘ LỌC VÀ TRỌNG SỐ SBERT
                    .sbertRejectionThreshold(0.40)
                    .sbertWeightTitle(0.45)
                    .sbertWeightSkills(0.40)
                    .sbertWeightExp(0.15)
                    .build();
            return repository.save(defaultSetting);
        });
        log.info("[AI CONFIG] Đã nạp cấu hình AI SBERT vào RAM.");
    }

    public AiSetting getSettings() {
        return currentSettings;
    }

    public AiSetting updateSettings(AiSetting newSettings) {
        newSettings.setId(1L);
        AiSetting saved = repository.save(newSettings);
        this.currentSettings = saved; // Cập nhật RAM ngay lập tức
        log.info("[AI CONFIG] Admin vừa cập nhật thông số hệ thống.");
        return saved;
    }
}