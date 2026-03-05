package com.xuanthi.talentmatchingbe.repository;

import com.xuanthi.talentmatchingbe.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);  //Optional giống như chiếc hộp có
    // thể chứa dữ liệu rỗng ( Buộc phải xử lý)
}
