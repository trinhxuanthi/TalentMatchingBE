package com.xuanthi.talentmatchingbe.repository;

import com.xuanthi.talentmatchingbe.entity.PricingPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PricingPlanRepository extends JpaRepository<PricingPlan, Long> {
    // Chỉ lôi ra các gói đang Active và đúng với Role của người dùng
    List<PricingPlan> findAllByTargetRoleAndIsActiveTrue(String targetRole);
}