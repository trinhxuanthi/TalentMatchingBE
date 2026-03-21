package com.xuanthi.talentmatchingbe.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xuanthi.talentmatchingbe.dto.job.JobRequest;
import com.xuanthi.talentmatchingbe.dto.job.JobResponse;
import com.xuanthi.talentmatchingbe.entity.Job;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Mapper(componentModel = "spring")
public abstract class JobMapper {

    // Không cần @Autowired ObjectMapper ở đây nữa để tránh lỗi Injection

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "employer", ignore = true)
    @Mapping(target = "categories", ignore = true)
    public abstract Job toEntity(JobRequest request);

    @Mapping(source = "employer.id", target = "employerId")
    @Mapping(source = "employer.fullName", target = "employerName")
    @Mapping(source = "employer.avatar", target = "employerAvatar")
    @Mapping(target = "categories", ignore = true)
    @Mapping(target = "mustHaveSkills", ignore = true)
    @Mapping(target = "niceToHaveSkills", ignore = true)
    @Mapping(target = "isExpired", expression = "java(job.getDeadline() != null && job.getDeadline().isBefore(java.time.LocalDateTime.now()))")
    public abstract JobResponse toResponse(Job job);

    // HÀM CẬP NHẬT (UPDATE) - Đảm bảo tên hàm và tham số chuẩn như sau:
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "employer", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "categories", ignore = true) // Sẽ xử lý thủ công ở Service hoặc AfterMapping
    public abstract void updateJobFromDto(JobRequest request, @MappingTarget Job job);

    @AfterMapping
    protected void enrichJobResponseWithJsonData(Job job, @MappingTarget JobResponse.JobResponseBuilder response) {
        // Lưu ý: @MappingTarget lúc này là JobResponseBuilder chứ không phải JobResponse
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

        try {
            if (job.getCategories() != null && !job.getCategories().isEmpty()) {
                List<String> cats = mapper.readValue(job.getCategories(), new TypeReference<List<String>>() {});
                response.categories(cats); // Dùng tên hàm của Builder (thường không có chữ 'set')
            }

            if (job.getRequiredSkills() != null && !job.getRequiredSkills().isEmpty()) {
                Map<String, Integer> skillMap = mapper.readValue(job.getRequiredSkills(), new TypeReference<Map<String, Integer>>() {});
                List<String> mustHave = new ArrayList<>();
                List<String> niceToHave = new ArrayList<>();

                skillMap.forEach((skill, weight) -> {
                    if (weight >= 3) mustHave.add(skill);
                    else niceToHave.add(skill);
                });
                response.mustHaveSkills(mustHave);
                response.niceToHaveSkills(niceToHave);
            }
        } catch (Exception e) {
            log.error("❌ Lỗi bóc JSON tại Mapper: {}", e.getMessage());
        }
    }
}