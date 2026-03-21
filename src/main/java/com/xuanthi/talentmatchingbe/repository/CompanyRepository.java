package com.xuanthi.talentmatchingbe.repository;

import com.xuanthi.talentmatchingbe.entity.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    // Tìm hồ sơ công ty theo ID của User
    Optional<Company> findByUserId(Long userId);

    // Lấy danh sách công ty theo trạng thái (Dùng cho Admin)
    List<Company> findByApprovalStatus(String approvalStatus);

    Page<Company> findByApprovalStatus(String approvalStatus, Pageable pageable);

    // Kiểm tra xem mã số thuế đã tồn tại chưa (Chống spam 1 cty đk nhiều tk)
    boolean existsByTaxCode(String taxCode);

    Page<Company> findByNameContainingIgnoreCaseAndApprovalStatus(String name, String approvalStatus, Pageable pageable);

    Optional<Company> findByIdAndApprovalStatus(Long id, String approvalStatus);
}