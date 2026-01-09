package com.example.monew.domain.notification.service;

import com.example.monew.domain.notification.dto.NotificationDto;
import com.example.monew.domain.notification.entity.Notifications;
import com.example.monew.domain.notification.repository.NotificationRepository;
import com.example.monew.domain.notification.response.CursorResponse;
import com.example.monew.global.exception.domain.notification.NotificationNotExistException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notiRepository;

    @Override
    public CursorResponse<NotificationDto> findAllNotificationByUserId(UUID userId, String cursor,
        Instant createdAt, int limit) {

        Pageable pageable = PageRequest.of(Integer.parseInt(cursor), limit, Direction.DESC);

        Slice<NotificationDto> sliceDto = notiRepository.findAllNotificationByUserId(userId, createdAt, pageable);

        Instant nextAfterCreatedAt = null;
        String nextCursorUUIDString = null;

        if (!sliceDto.getContent().isEmpty()) {
            NotificationDto notificationDto = sliceDto.getContent().get(sliceDto.getContent().size() - 1);
            nextAfterCreatedAt = notificationDto.createdAt();
            nextCursorUUIDString = notificationDto.id().toString();
        }

        CursorResponse<NotificationDto> notiDto = NotificationDto.from(sliceDto, nextCursorUUIDString, nextAfterCreatedAt);

        log.info("✅ findAllNotificationByUserId.notiDto = " + notiDto);

        return notiDto;
    }

    @Transactional
    @Override
    public void allCheckNotification(UUID userId) {
        notiRepository.findAllByUserId(userId)
            .stream()
            .peek(noti -> log.info("✅ allCheckNotification.userId = " + userId.toString()))
            .forEach(noti -> noti.checkNotificationRead(userId));
    }

    @Transactional
    @Override
    public void checkNotification(UUID notificationId, UUID userId) {
        Notifications notifications = notiRepository.findByIdAndUserId(notificationId, userId)
            .orElseThrow(() -> new NotificationNotExistException(notificationId, userId));

        notifications.checkNotificationRead(userId);

        log.info("✅ allCheckNotification.userId = " + userId.toString());
    }

    @Transactional
    @Override
    public void deleteNotificationInBatch() {
        // 확인한 알림 중 1주일이 경과된 알림은 자동으로 삭제됩니다.
        Instant oneWeekAgo = Instant.now().minus(7, ChronoUnit.DAYS);

        List<Notifications> notificationsList = notiRepository.findBatchDeleteNotification(oneWeekAgo);
        System.out.println("notificationsList = " + notificationsList);

        if (!notificationsList.isEmpty()) {
            notiRepository.deleteAll(notificationsList);
            log.info("⏰ NotificationDeleteScheduler ⭕️ - 노티 배치 삭제 완료");
        }
        else {
            log.info("⏰ NotificationDeleteScheduler ❌️ -  삭제할 노티 없음");
        }
    }
}
