package com.xuanthi.talentmatchingbe.mapper;

import com.xuanthi.talentmatchingbe.dto.auth.RegisterRequest;
import com.xuanthi.talentmatchingbe.dto.user.UserResponse;
import com.xuanthi.talentmatchingbe.dto.user.UserUpdateRequest;
import com.xuanthi.talentmatchingbe.entity.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")    // Tạo ra cái class thực thi này như là một Spring Bean
public interface UserMapper {
    @Mapping(target = "isActive", constant = "true")
    UserResponse toUserResponse(User user);

    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "provider", constant = "LOCAL")
    @Mapping(target = "id", ignore = true)
    User toUser(RegisterRequest request);

    //Cập nhật dữ liệu từ DTO vào Entity có sẵn
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUserDetails(UserUpdateRequest request, @MappingTarget User user);
}