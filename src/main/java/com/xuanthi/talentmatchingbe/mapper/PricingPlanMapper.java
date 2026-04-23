package com.xuanthi.talentmatchingbe.mapper;

import com.xuanthi.talentmatchingbe.dto.payment.PricingPlanRequest;
import com.xuanthi.talentmatchingbe.dto.payment.PricingPlanResponse;
import com.xuanthi.talentmatchingbe.entity.PricingPlan;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PricingPlanMapper {

    // Map từ Entity sang Response
    // MapStruct tự gọi hàm getFinalPrice() của sếp để đắp vào finalPrice
    @Mapping(target = "finalPrice", expression = "java(plan.getFinalPrice())")
    PricingPlanResponse toResponse(PricingPlan plan);

    // Map từ Request lúc Tạo mới sang Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isActive", constant = "true") // Mặc định tạo ra là mở bán luôn
    @Mapping(target = "createdAt", ignore = true)
    PricingPlan toEntity(PricingPlanRequest request);

    // Map từ Request lúc Update đè vào Entity cũ
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true) // Không update status ở hàm này
    @Mapping(target = "createdAt", ignore = true)
    void updateEntityFromRequest(PricingPlanRequest request, @MappingTarget PricingPlan plan);
}