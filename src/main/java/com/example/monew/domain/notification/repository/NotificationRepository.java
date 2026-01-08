package com.example.monew.domain.notification.repository;

import com.example.monew.domain.notification.dto.NotificationDto;
import com.example.monew.domain.notification.entity.Notification;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    // 확인한 알림 중 1주일이 경과된 알림은 자동으로 삭제됩니다
    @Query("SELECT n FROM Notification n "
        + "WHERE n.isRead = true AND n.createdAt < :oneWeekAgoDate")
    List<Notification> findBatchDeleteNotification(@Param("oneWeekAgoDate") LocalDateTime oneWeekAgoDate);

    // 확인하지 않은 알림만 조회합니다.
    // 시간 순으로 정렬 및 커서 페이지네이션을 구현합니다.
    @Query("SELECT n FROM Notification n "
        + "JOIN FETCH n.user u "
        + "WHERE u.id = :userId AND n.isRead = false "
        + "ORDER BY n.createdAt")
    Slice<NotificationDto> findAllNotificationByUserId(@Param("userId") UUID userId,
        @Param("createAt") Instant createAt,
        Pageable pageable);

    List<Notification> findAllByUserId(UUID userId);
    Optional<Notification> findAllByIdAndUserId(UUID notificationId, UUID userId);
}
