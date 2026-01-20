package com.example.monew.domain.notification.controller;

import com.example.monew.domain.notification.dto.NotificationDto;
import com.example.monew.domain.notification.response.CursorResponse;
import com.example.monew.domain.notification.service.NotificationService;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.UUID;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController implements NotificationControllerDocs {
    private final NotificationService service;

    @GetMapping
    public ResponseEntity<CursorResponse<NotificationDto>> findAllByUserId(
        @RequestParam(required = false) String cursor,
        @RequestParam(required = false) Instant after,          // 보조 커서(createdAt) 값
        @RequestParam(defaultValue = "50") @Positive int limit,    // 커서 페이지 크기
        @RequestHeader("Monew-Request-User-ID") @NotNull UUID userId   // 요청자 ID
    ) {
        // 알림 목록을 조회합니다.

        CursorResponse<NotificationDto> cursorResponse = service.findAllByUserId(userId, cursor, after, limit);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(cursorResponse);
    }

    @PatchMapping
    public ResponseEntity<Void> allCheckNotification(
        @RequestHeader("Monew-Request-User-ID") @NotNull UUID userId) {
        // 전체 알림을 한번에 확인합니다.

        service.allCheckNotification(userId);

        return ResponseEntity
            .status(HttpStatus.OK)
            .build();
    }

    @PatchMapping("/{notificationId}")
    public ResponseEntity<Void> checkNotification(
        @PathVariable UUID notificationId,
        @RequestHeader("Monew-Request-User-ID") @NotNull UUID userId) {
        // 알림을 확인합니다.

         service.checkNotification(notificationId, userId);

         return ResponseEntity
             .status(HttpStatus.OK)
             .build();
    }
}
