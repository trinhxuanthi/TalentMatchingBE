package com.xuanthi.talentmatchingbe.service;

import com.xuanthi.talentmatchingbe.entity.ProFeatureLog;
import com.xuanthi.talentmatchingbe.entity.User;
import com.xuanthi.talentmatchingbe.enums.ProFeature;
import com.xuanthi.talentmatchingbe.repository.ProFeatureLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProService {

    private final ProFeatureLogRepository logRepository;

    /**
     * Gác cổng: Chặn Basic, Ghi sổ Pro
     */
    public void validateAndLogUsage(User user, ProFeature feature) {
        // 1. Nếu là BASIC -> Đuổi cổ
        if (!user.isPro()) {
            log.warn("User ID {} cố tình dùng tính năng PRO: {}", user.getId(), feature.name());
            throw new AccessDeniedException("Tính năng này chỉ dành cho tài khoản PRO. Vui lòng nâng cấp gói cước!");
        }

        // 2. Nếu là PRO -> Ghi sổ vào DB
        ProFeatureLog featureLog = ProFeatureLog.builder()
                .user(user)
                .featureName(feature)
                .build();

        logRepository.save(featureLog);
        log.info("User ID {} (PRO) đã dùng tính năng: {}", user.getId(), feature.name());
    }
}