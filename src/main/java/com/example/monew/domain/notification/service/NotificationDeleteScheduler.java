package com.example.monew.domain.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationDeleteScheduler {
    static final long BATCH_INTERVAL = 1000 * 60 * 60 * 24; // 매일 호출

    private final NotificationService notiService;

    public void deleteNotificationInBatch() {
        notiService.deleteNotificationInBatch();
    }
}
