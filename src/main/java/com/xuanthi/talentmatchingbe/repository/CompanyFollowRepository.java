package com.xuanthi.talentmatchingbe.repository;

import com.xuanthi.talentmatchingbe.entity.Company;
import com.xuanthi.talentmatchingbe.entity.CompanyFollow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyFollowRepository extends JpaRepository<CompanyFollow, Long> {

    // Tìm xem record follow đã tồn tại chưa để toggle (Bật/Tắt)
    Optional<CompanyFollow> findByUserIdAndCompanyId(Long userId, Long companyId);

    // Truy vấn tối ưu: Lấy thẳng danh sách công ty mà user đang theo dõi
    @Query("SELECT cf.company FROM CompanyFollow cf WHERE cf.user.id = :userId")
    Page<Company> findFollowedCompaniesByUserId(@Param("userId") Long userId, Pageable pageable);
}
