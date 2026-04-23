package com.xuanthi.talentmatchingbe.mapper;

import com.xuanthi.talentmatchingbe.dto.application.ApplicationResponse;
import com.xuanthi.talentmatchingbe.dto.application.ApplicationSimpleResponse;
import com.xuanthi.talentmatchingbe.dto.application.CandidateApplyRequest;
import com.xuanthi.talentmatchingbe.entity.Application;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring")
public interface ApplicationMapper {

    // ==========================================
    // 1. TU REQUEST -> ENTITY (Dung khi nop CV)
    // ==========================================
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "job", ignore = true)
    @Mapping(target = "candidate", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "matchScore", ignore = true)
    @Mapping(target = "aiAnalysis", ignore = true)
    @Mapping(target = "aiRecommendation", ignore = true) // Them truong nay: Bo qua vi luc moi nop don AI chua cham
    @Mapping(target = "isAiScored", ignore = true)
    @Mapping(target = "notes", ignore = true)
    @Mapping(target = "appliedAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Application toEntity(CandidateApplyRequest request);

    // ==========================================
    // 2. TU ENTITY -> RESPONSE CHI TIET (Dung cho API Detail /detail/{id})
    // ==========================================
    @Mapping(target = "jobId", source = "job.id")
    @Mapping(target = "jobTitle", source = "job.title")
    @Mapping(target = "candidateId", source = "candidate.id")
    @Mapping(target = "candidateFullName", source = "fullName")
    @Mapping(target = "candidateEmail", source = "email")
    @Mapping(target = "isCandidatePro", expression = "java(application.getCandidate() != null && application.getCandidate().isPro())")
    // FIX QUAN TRONG: Đã xóa dòng @Mapping(target = "aiRecommendation", ignore = true)
    // MapStruct se tu dong map aiRecommendation va aiAnalysis tu Entity sang DTO vi chung da trung ten.
    ApplicationResponse toResponse(Application application);

    // ==========================================
    // 3. TU ENTITY -> RESPONSE MONG NHE (Dung cho API List phan trang)
    // ==========================================
    @Mapping(target = "candidateId", source = "candidate.id")
    @Mapping(target = "candidateFullName", source = "fullName")
    @Mapping(target = "candidateEmail", source = "email")
    @Mapping(target = "isCandidatePro", expression = "java(application.getCandidate() != null && application.getCandidate().isPro())")
    ApplicationSimpleResponse toSimpleResponse(Application application);

    // ==========================================
    // 4. CHUYEN DANH SACH
    // ==========================================
    default Page<ApplicationResponse> toResponsePage(Page<Application> page) {
        return page.map(this::toResponse);
    }
}