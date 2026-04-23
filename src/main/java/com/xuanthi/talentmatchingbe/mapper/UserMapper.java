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

/**
 * MapStruct mapper cho User entity và các DTO liên quan
 * Xử lý việc chuyển đổi giữa User entity và các DTO request/response
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    /**
     * Chuyển đổi User entity sang UserResponse DTO
     * @param user User entity
     * @return UserResponse DTO với isActive = true
     */
    @Mapping(target = "isActive", constant = "true")
    UserResponse toUserResponse(User user);

    /**
     * Chuyển đổi RegisterRequest DTO sang User entity
     * @param request RegisterRequest DTO
     * @return User entity với các giá trị mặc định
     */
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "provider", constant = "LOCAL")
    @Mapping(target = "id", ignore = true)
    User toUser(RegisterRequest request);

    /**
     * Cập nhật thông tin User entity từ UserUpdateRequest DTO
     * Chỉ cập nhật các trường không null trong request
     * @param request UserUpdateRequest chứa thông tin cần cập nhật
     * @param user User entity cần cập nhật
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUserDetails(UserUpdateRequest request, @MappingTarget User user);
}