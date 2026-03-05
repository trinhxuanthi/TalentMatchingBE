package com.xuanthi.talentmatchingbe.mapper;

import com.xuanthi.talentmatchingbe.dto.user.UserResponse;
import com.xuanthi.talentmatchingbe.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")    // Tạo ra cái class thực thi này như là một Spring Bean
public interface UserMapper {
    UserResponse toResponse(User user);
}