package com.xuanthi.talentmatchingbe.repository;

import com.xuanthi.talentmatchingbe.entity.User;
import com.xuanthi.talentmatchingbe.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);  //Optional giống như chiếc hộp có
    // thể chứa dữ liệu rỗng ( Buộc phải xử lý)

    // Tối ưu: Chỉ lấy những User đang hoạt động (cho luồng Login)
    Optional<User> findByEmailAndIsActiveTrue(String email);

    // Tìm kiếm Employer theo tên để hiện danh sách công ty
    List<User> findAllByRoleAndIsActiveTrue(Role role);
}
