package com.xuanthi.talentmatchingbe.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "company_follows", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "company_id"}) // Chống lỗi 1 người follow 1 công ty 2 lần
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyFollow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Dùng LAZY để khi query bảng này, nó không tự động móc toàn bộ thông tin User lên làm chậm hệ thống
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;
}