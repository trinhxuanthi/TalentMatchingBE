package com.xuanthi.talentmatchingbe.mapper;

import com.xuanthi.talentmatchingbe.dto.job.JobRequest;
import com.xuanthi.talentmatchingbe.dto.job.JobResponse;
import com.xuanthi.talentmatchingbe.entity.Job;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface JobMapper {

    Job toEntity(JobRequest request);

    // Chuyển từ Entity sang Response (Bổ sung thông tin Employer)
    @Mapping(source = "employer.id", target = "employerId")
    @Mapping(source = "employer.fullName", target = "employerName")
    @Mapping(source = "employer.avatar", target = "employerAvatar")
    //Hiện trạng thái hết hạn ở Frontend:
    @Mapping(target = "isExpired", expression = "java(job.getDeadline() != null && job.getDeadline().isBefore(java.time.LocalDateTime.now()))")
    JobResponse toResponse(Job job);

    /**
     * Cập nhật dữ liệu từ Request vào Entity hiện có
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "employer", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "status", ignore = true) // Thường Status sẽ được update qua API riêng (đóng/mở job)
    void updateJobFromDto(JobRequest request, @MappingTarget Job job);
}