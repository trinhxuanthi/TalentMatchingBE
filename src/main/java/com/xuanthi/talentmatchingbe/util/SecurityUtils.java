package com.xuanthi.talentmatchingbe.util;

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
}