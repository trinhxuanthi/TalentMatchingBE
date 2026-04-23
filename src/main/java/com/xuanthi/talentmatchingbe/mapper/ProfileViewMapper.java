package com.xuanthi.talentmatchingbe.mapper;

import com.xuanthi.talentmatchingbe.dto.user.ProfileViewResponse;
import com.xuanthi.talentmatchingbe.entity.ProfileView;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProfileViewMapper {

    @Mapping(source = "employer.id", target = "employerId")
    @Mapping(source = "employer.fullName", target = "employerName") // Tên ông HR

    @Mapping(source = "employer.company.id", target = "companyId")
    @Mapping(source = "employer.company.name", target = "companyName")
    @Mapping(source = "employer.company.logoUrl", target = "companyLogo") // Hoặc imageUrl tùy sếp đặt

    ProfileViewResponse toResponse(ProfileView profileView);
}