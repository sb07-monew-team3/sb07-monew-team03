package com.example.monew.domain.notification.controller;

import com.example.monew.domain.notification.dto.NotificationDto;
import com.example.monew.domain.notification.response.CursorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.Instant;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "알림 관리", description = "알림 관련 API")
public interface NotificationControllerDocs {

    @Operation(summary = "알림 목록 조회", description = "알림 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (정렬 기준 오류, 페이지네이션 파라미터 오류 등)"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류"),
        @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    ResponseEntity<CursorResponse<NotificationDto>> findAllByUserId(
        @RequestParam(required = false) String cursor,
        @RequestParam(required = false) Instant after,          // 보조 커서(createdAt) 값
        @RequestParam(defaultValue = "50") @Positive int limit,    // 커서 페이지 크기
        @RequestHeader("Monew-Request-User-ID") @NotNull UUID userId   // 요청자 ID
    );

    @Operation(summary = "전체 알림 확인", description = "전체 알림을 한번에 확인합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "전체 알림 확인 성공"),
        @ApiResponse(responseCode = "404", description = "사용자 정보 없음"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (입력값 검증 실패)")
    })
    ResponseEntity<Void> allCheckNotification(
        @RequestHeader("Monew-Request-User-ID") @NotNull UUID userId);

    @Operation(summary = "알림 확인", description = "알림을 확인합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "알림 확인 성공"),
        @ApiResponse(responseCode = "404", description = "사용자 정보 없음"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (입력값 검증 실패)")
    })
    ResponseEntity<Void> checkNotification(
        @PathVariable UUID notificationId,
        @RequestHeader("Monew-Request-User-ID") @NotNull UUID userId);
}
