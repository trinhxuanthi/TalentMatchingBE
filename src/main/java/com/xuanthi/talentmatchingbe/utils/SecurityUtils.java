package com.xuanthi.talentmatchingbe.utils;

import com.xuanthi.talentmatchingbe.entity.User;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    // Lấy đối tượng User đang đăng nhập từ SecurityContext
    public static User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof User) {
            return (User) principal;
        }
        return null;
    }
    public static User getRequiredCurrentUser() {
        User user = getCurrentUser(); // Gọi lại hàm cũ của sếp
        if (user == null) {
            // Ném lỗi để GlobalExceptionHandler của sếp hốt gọn
            throw new RuntimeException("Vui lòng đăng nhập để thực hiện hành động này!");
        }
        return user;
    }
}