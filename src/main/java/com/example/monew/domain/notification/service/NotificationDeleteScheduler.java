package com.example.monew.domain.notification.service;

import com.example.monew.domain.notification.entity.Notification;
import com.example.monew.domain.notification.repository.NotificationRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationDeleteScheduler {
    static final long BATCH_INTERVAL = 1000 * 60 * 60 * 24; // 매일 호출

    private final NotificationRepository notificationRepository;

    @Scheduled(fixedRate = BATCH_INTERVAL)
    public void deleteNotification() {
        // 확인한 알림 중 1주일이 경과된 알림은 자동으로 삭제됩니다.
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);
        List<Notification> notificationList = notificationRepository.findBatchDeleteNotification(oneWeekAgo);

        if (!notificationList.isEmpty()) {
            notificationRepository.deleteAll(notificationList);
            log.info("⏰ NotificationDeleteScheduler ⭕️");
        }
        else {
            log.info("⏰ NotificationDeleteScheduler ❌️ -  삭제할 노티 없음");
        }
    }
}
