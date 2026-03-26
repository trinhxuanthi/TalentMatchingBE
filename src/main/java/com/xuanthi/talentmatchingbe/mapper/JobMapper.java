package com.xuanthi.talentmatchingbe.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "employer", ignore = true)
    @Mapping(target = "categories", ignore = true)
    public abstract Job toEntity(JobRequest request);

    @Mapping(source = "employer.id", target = "employerId")
    @Mapping(source = "employer.fullName", target = "employerName")
    @Mapping(source = "employer.avatar", target = "employerAvatar")
    // THÊM ĐÚNG DÒNG NÀY ĐỂ LẤY EMAIL CHO BẠN FRONTEND LÀM CHAT NÈ:
    @Mapping(source = "employer.email", target = "employerEmail")
    @Mapping(target = "categories", ignore = true)
    @Mapping(target = "mustHaveSkills", ignore = true)
    @Mapping(target = "niceToHaveSkills", ignore = true)
    @Mapping(target = "isExpired", expression = "java(job.getDeadline() != null && job.getDeadline().isBefore(java.time.LocalDateTime.now()))")
    public abstract JobResponse toResponse(Job job);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "employer", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "categories", ignore = true)
    public abstract void updateJobFromDto(JobRequest request, @MappingTarget Job job);

    @AfterMapping
    protected void enrichJobResponseWithJsonData(Job job, @MappingTarget JobResponse response) {
        try {
            if (job.getCategories() != null && !job.getCategories().isEmpty()) {
                List<String> cats = MAPPER.readValue(job.getCategories(), new TypeReference<List<String>>() {});
                response.setCategories(cats);
            }

            if (job.getRequiredSkills() != null && !job.getRequiredSkills().isEmpty()) {
                Map<String, Integer> skillMap = MAPPER.readValue(job.getRequiredSkills(), new TypeReference<Map<String, Integer>>() {});
                List<String> mustHave = new ArrayList<>();
                List<String> niceToHave = new ArrayList<>();

                skillMap.forEach((skill, weight) -> {
                    if (weight >= 3) mustHave.add(skill);
                    else niceToHave.add(skill);
                });

                response.setMustHaveSkills(mustHave);
                response.setNiceToHaveSkills(niceToHave);
            }
        } catch (Exception e) {
            log.error("❌ Lỗi bóc JSON tại Mapper (Job ID: {}): {}", job.getId(), e.getMessage());
        }
    }
}