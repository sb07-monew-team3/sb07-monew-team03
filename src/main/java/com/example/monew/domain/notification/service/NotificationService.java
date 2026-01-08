package com.example.monew.domain.notification.service;

import com.example.monew.domain.notification.dto.NotificationDto;
import com.example.monew.domain.notification.response.CursorResponse;
import java.time.Instant;
import java.util.UUID;

public interface NotificationService {
    CursorResponse<NotificationDto> findAllNotificationByUserId( UUID userId, String cursor, Instant after, int limit);
    void allCheckNotification( UUID userId);
    void checkNotification( UUID notificationId, UUID userId);
}
