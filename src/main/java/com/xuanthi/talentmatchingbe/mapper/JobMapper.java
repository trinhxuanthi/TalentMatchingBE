package com.xuanthi.talentmatchingbe.mapper;

import com.xuanthi.talentmatchingbe.dto.job.JobRequest;
import com.xuanthi.talentmatchingbe.dto.job.JobResponse;
import com.xuanthi.talentmatchingbe.entity.Job;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

// Không cần abstract class hay ObjectMapper nữa, dùng interface là đủ!
@Mapper(componentModel = "spring", collectionMappingStrategy = CollectionMappingStrategy.ACCESSOR_ONLY)
public interface JobMapper {

    // ==========================================
    // 1. TỪ REQUEST -> ENTITY (TẠO MỚI)
    // ==========================================
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "employer", ignore = true) // Set tay bên Service
    @Mapping(target = "status", ignore = true)   // Set tay mặc định OPEN
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    // MapStruct tự động map List<String> requiredSkills và categories ngon lành!
    Job toEntity(JobRequest request);

    // ==========================================
    // 2. TỪ ENTITY -> RESPONSE (TRẢ VỀ FRONTEND)
    // ==========================================
    @Mapping(source = "employer.id", target = "employerId")
    @Mapping(source = "employer.fullName", target = "employerName")
    @Mapping(source = "employer.avatar", target = "employerAvatar")
    @Mapping(source = "employer.email", target = "employerEmail")
    // Dùng biểu thức tính toán xem tin đã hết hạn chưa
    @Mapping(source = "aiJobSetting", target = "aiSettings")
    @Mapping(source = "priority", target = "isPriority")
    @Mapping(target = "isExpired", expression = "java(job.getDeadline() != null && job.getDeadline().isBefore(java.time.LocalDateTime.now()))")
    JobResponse toResponse(Job job);

    // ==========================================
    // 3. CẬP NHẬT ENTITY TỪ DTO (UPDATE JOB)
    // ==========================================
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "employer", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "requiredSkills", ignore = true)
    @Mapping(target = "categories", ignore = true)
    void updateJobFromDto(JobRequest request, @MappingTarget Job job);
}