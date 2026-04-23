package com.xuanthi.talentmatchingbe.repository;

import com.xuanthi.talentmatchingbe.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param; // ✅ Thêm import này
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // 1. Lấy thông báo của 1 người, ưu tiên thông báo mới nhất
    Page<Notification> findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // 2. Đếm số thông báo chưa đọc để hiện số (badge) đỏ trên icon chuông
    long countByUserIdAndIsReadFalseAndIsDeletedFalse(Long userId);

    // 3. Đánh dấu tất cả là đã đọc bằng 1 câu SQL duy nhất
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user.id = :userId AND n.isRead = false")
    void markAllAsRead(@Param("userId") Long userId); // ✅ Gắn @Param cho an toàn tuyệt đối

    // ==========================================
    // 🎁 TẶNG THÊM 2 HÀM (Cực kỳ cần thiết cho Frontend):
    // ==========================================

    // 4. Đánh dấu 1 thông báo cụ thể là đã đọc (Khi user click vào)
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.id = :notiId AND n.user.id = :userId")
    void markOneAsRead(@Param("notiId") Long notiId, @Param("userId") Long userId);

    // 5. Xóa mềm (Soft Delete) 1 thông báo
    @Modifying
    @Query("UPDATE Notification n SET n.isDeleted = true WHERE n.id = :notiId AND n.user.id = :userId")
    void softDeleteNotification(@Param("notiId") Long notiId, @Param("userId") Long userId);
}