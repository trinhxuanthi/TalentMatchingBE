package com.xuanthi.talentmatchingbe.service;

import com.xuanthi.talentmatchingbe.dto.payment.PricingPlanRequest;
import com.xuanthi.talentmatchingbe.dto.payment.PricingPlanResponse;
import com.xuanthi.talentmatchingbe.entity.PricingPlan;
import com.xuanthi.talentmatchingbe.mapper.PricingPlanMapper;
import com.xuanthi.talentmatchingbe.repository.PricingPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminPricingPlanService {

    private final PricingPlanRepository planRepository;
    private final PricingPlanMapper planMapper;

    // 1. XEM TẤT CẢ GÓI (Cả đang bán và ngừng bán)
    public List<PricingPlanResponse> getAllPlans() {
        return planRepository.findAll().stream()
                .map(planMapper::toResponse)
                .collect(Collectors.toList());
    }

    // 2. TẠO GÓI MỚI (Ví dụ: Gói Siêu VIP dịp Tết)
    @Transactional
    public PricingPlanResponse createPlan(PricingPlanRequest request) {
        PricingPlan newPlan = planMapper.toEntity(request);
        PricingPlan savedPlan = planRepository.save(newPlan);
        log.info("Admin vừa tạo gói cước mới: {}", savedPlan.getName());
        return planMapper.toResponse(savedPlan);
    }

    // 3. CẬP NHẬT GIÁ / GIẢM GIÁ
    @Transactional
    public PricingPlanResponse updatePlan(Long id, PricingPlanRequest request) {
        PricingPlan plan = planRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy gói cước!"));

        // Đổ dữ liệu mới vào Entity cũ
        planMapper.updateEntityFromRequest(request, plan);

        PricingPlan updatedPlan = planRepository.save(plan);
        log.info("Admin vừa cập nhật gói cước ID: {}", id);
        return planMapper.toResponse(updatedPlan);
    }

    // 4. BẬT/TẮT TRẠNG THÁI (Ngừng bán gói cũ)
    @Transactional
    public void togglePlanStatus(Long id) {
        PricingPlan plan = planRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy gói cước!"));

        plan.setActive(!plan.isActive()); // Đảo ngược trạng thái
        planRepository.save(plan);
        log.info("Gói cước ID: {} đã chuyển trạng thái thành: {}", id, plan.isActive() ? "MỞ BÁN" : "NGỪNG BÁN");
    }
}