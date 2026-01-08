package com.example.monew.domain.notification.service;

import com.example.monew.domain.notification.dto.NotificationDto;
import com.example.monew.domain.notification.entity.Notifications;
import com.example.monew.domain.notification.repository.NotificationRepository;
import com.example.monew.domain.notification.response.CursorResponse;
import java.time.Instant;
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
@Transactional
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notiRepository;

    @Override
    public CursorResponse<NotificationDto> findAllNotificationByUserId(UUID userId, String cursor,
        Instant createdAt, int limit) {

        Pageable pageable = PageRequest.of(Integer.parseInt(cursor), limit, Direction.DESC);

        Slice<NotificationDto> sliceDto = notiRepository.findAllNotificationByUserId(userId, createdAt, pageable);

        Instant nextAfterCreatedAt = null;
        UUID nextCursorUUID = null;

        if (!sliceDto.getContent().isEmpty()) {
            NotificationDto notificationDto = sliceDto.getContent().get(sliceDto.getContent().size() - 1);
            nextAfterCreatedAt = notificationDto.createdAt();
            nextCursorUUID = notificationDto.id();
        }

        CursorResponse<NotificationDto> notiDto = new CursorResponse<>(sliceDto.getContent(),
                nextCursorUUID.toString(),
                nextAfterCreatedAt,
                sliceDto.getSize(),
                null,
                sliceDto.hasNext());

        log.info("✅ findAllNotificationByUserId.notiDto = " + notiDto.toString());

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
        Notifications notifications = notiRepository.findAllByIdAndUserId(notificationId, userId)
            .orElseThrow(() -> new IllegalArgumentException(
                "🚨 checkNotification.notificationId = " + notificationId.toString() + "/ userId = "
                    + userId.toString()));

        notifications.checkNotificationRead(userId);

        log.info("✅ allCheckNotification.userId = " + userId.toString());
    }
}
