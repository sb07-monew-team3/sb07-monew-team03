package com.example.monew.domain.notification.service;

import com.example.monew.domain.interest.entity.Interest;
import com.example.monew.domain.notification.dto.NotificationDto;
import com.example.monew.domain.notification.response.CursorResponse;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public interface NotificationService {
    CursorResponse<NotificationDto> findAllByUserId( UUID userId, String cursor, Instant after, int limit);
    void allCheckNotification( UUID userId);
    void checkNotification( UUID notificationId, UUID userId);
    void notifyCommentLiked(UUID receiverId, String actorNickname, UUID commentId);
    void createInterestAlarm(Map<Interest, Integer> interestList);
    void deleteNotificationInBatch();
}
