package com.xuanthi.talentmatchingbe.mapper;

import com.xuanthi.talentmatchingbe.dto.application.ApplicationResponse;
import com.xuanthi.talentmatchingbe.entity.Application;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring")
public interface ApplicationMapper {

    // Chuyển từ Entity sang Response (DTO)
    @Mapping(target = "jobId", source = "job.id")
    @Mapping(target = "jobTitle", source = "job.title")
    @Mapping(target = "candidateId", source = "candidate.id")
    @Mapping(target = "candidateFullName", source = "candidate.fullName")
    @Mapping(target = "candidateEmail", source = "candidate.email")
    ApplicationResponse toResponse(Application application);

    // Chuyển danh sách (Dùng cho Phân trang)
    default Page<ApplicationResponse> toResponsePage(Page<Application> page) {
        return page.map(this::toResponse);
    }
}