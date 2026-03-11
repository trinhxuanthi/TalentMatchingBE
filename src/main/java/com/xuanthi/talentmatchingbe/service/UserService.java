package com.xuanthi.talentmatchingbe.service;

import com.xuanthi.talentmatchingbe.dto.user.UserResponse;
import com.xuanthi.talentmatchingbe.dto.user.UserUpdateRequest;
import com.xuanthi.talentmatchingbe.entity.User;
import com.xuanthi.talentmatchingbe.mapper.UserMapper;
import com.xuanthi.talentmatchingbe.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserResponse getMyProfile() {
        // Lấy email từ SecurityContext (nơi mà JwtFilter đã lưu sau khi giải mã token)
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmailAndIsActiveTrue(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return userMapper.toUserResponse(user);
    }

    public UserResponse updateProfile(UserUpdateRequest request) {
        // Lấy email từ SecurityContext
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        // Tìm User trong DB
        User user = userRepository.findByEmailAndIsActiveTrue(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // MapStruct tự động copy fullName từ request sang user
        userMapper.updateUserDetails(request, user);

        //Lưu lại
        User updatedUser = userRepository.save(user);

        return userMapper.toUserResponse(updatedUser);
    }
}