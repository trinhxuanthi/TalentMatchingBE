package com.xuanthi.talentmatchingbe.entity;

import com.xuanthi.talentmatchingbe.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_email", columnList = "email"),
        @Index(name = "idx_user_role", columnList = "role")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 191) // Độ dài tối ưu cho MySQL Index
    private String email;

    @Column(nullable = true)
    private String password;

    @Column(name = "fullname")
    private String fullName;

    @Column(name = "avatar", length = 500)
    private String avatar;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "is_active")
    @Builder.Default
    private boolean isActive = true;

    @Column(name = "provider", length = 20)
    @Builder.Default
    private String provider = "LOCAL";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Role role;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // TỐI ƯU: mappedBy phải khớp với tên biến 'employer' bên class Job
    // cascade = ALL: Nếu xóa User thì xóa luôn các Job của họ (tùy nghiệp vụ)
    // orphanRemoval: Tự động dọn dẹp các Job không còn chủ sở hữu
    @OneToMany(mappedBy = "employer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Job> jobs;

    // --- CÁC PHƯƠNG THỨC TỐI ƯU CỦA USERDETAILS ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return role != null
                ? List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))
                : List.of();
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isEnabled() {
        // TỐI ƯU: Nếu isActive = false, Spring Security sẽ chặn đăng nhập ngay lập tức
        return this.isActive;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() {
        // Bạn có thể dùng isActive cho cả việc khóa tài khoản
        return this.isActive;
    }

    @Override
    public boolean isCredentialsNonExpired() { return true; }
}