package com.xuanthi.talentmatchingbe.repository;

import com.xuanthi.talentmatchingbe.entity.User;
import com.xuanthi.talentmatchingbe.enums.AccountType;
import com.xuanthi.talentmatchingbe.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository cho User entity
 * Quản lý các thao tác database liên quan đến user
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Kiểm tra xem email đã tồn tại trong hệ thống chưa
     * @param email email cần kiểm tra
     * @return true nếu email đã tồn tại, false nếu chưa
     */
    boolean existsByEmail(String email);

    /**
     * Tìm user theo email (có thể inactive)
     * @param email email của user
     * @return Optional chứa User nếu tìm thấy
     */
    Optional<User> findByEmail(String email);

    /**
     * Tìm user theo email và chỉ lấy những user đang active
     * Tối ưu cho việc login và các thao tác yêu cầu user active
     * @param email email của user
     * @return Optional chứa User nếu tìm thấy và đang active
     */
    Optional<User> findByEmailAndIsActiveTrue(String email);

    /**
     * Tìm tất cả user theo role và chỉ lấy những user đang active
     * Dùng để lấy danh sách employer cho việc hiển thị công ty
     * @param role role của user (EMPLOYER, CANDIDATE, ADMIN)
     * @return List các User có role tương ứng và đang active
     */
    List<User> findAllByRoleAndIsActiveTrue(Role role);

    /**
     * Đếm số lượng user theo role
     * @param role role cần đếm
     * @return số lượng user có role tương ứng
     */
    long countByRole(Role role);

    /**
     * Tìm user theo phone number
     * @param phoneNumber số điện thoại
     * @return Optional chứa User nếu tìm thấy
     */
    Optional<User> findByPhoneNumber(String phoneNumber);

    // ✅ THÊM DÒNG NÀY VÀO: Tìm các User có AccountType cụ thể VÀ ngày hết hạn nhỏ hơn một thời điểm
    List<User> findByAccountTypeAndProExpiredAtBefore(AccountType accountType, LocalDateTime time);
    @Query("SELECT role, COUNT(u) FROM User u GROUP BY role")
    List<Object[]> countUsersByRole(); // Đếm xem có bao nhiêu HR, bao nhiêu Candidate
}
