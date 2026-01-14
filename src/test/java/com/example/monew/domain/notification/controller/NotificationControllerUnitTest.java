package com.example.monew.domain.notification.controller;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

import com.example.monew.domain.notification.dto.NotificationDto;
import com.example.monew.domain.notification.entity.ResourceType;
import com.example.monew.domain.notification.response.CursorResponse;
import com.example.monew.domain.notification.service.NotificationService;
import com.example.monew.global.exception.CommonExceptionHandler;
import com.example.monew.global.exception.CustomException;
import com.example.monew.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(NotificationController.class)
@Import(CommonExceptionHandler.class)
class NotificationControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private NotificationService notiService;

    private UUID userId;
    private UUID notificationId;
    private final int limit = 50;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        notificationId = UUID.randomUUID();
    }

    @Test
    @DisplayName("알림 목록 조회 O - 200 조회 성공")
    void findAllNotificationByUserId_OK() throws Exception {

    UUID resourceId = UUID.randomUUID();
    Instant now = Instant.now();

    NotificationDto notificationDto = NotificationDto.builder()
        .id(notificationId)
        .createdAt(now)
        .updatedAt(now)
        .confirmed(true)
        .userId(userId)
        .content("test_OK")
        .resourceType(ResourceType.INTEREST)
        .resourceId(resourceId)
        .build();

    CursorResponse<NotificationDto> response =
        new CursorResponse<>(
            List.of(notificationDto),
            null,
            null,
            limit,
            1L,
            false
        );

    given(notiService.findAllByUserId(userId, null, null, limit))
        .willReturn(response);

    // when & then
    mockMvc.perform(get("/notifications")
            .param("limit", String.valueOf(limit))
            .header("Monew-Request-User-ID", userId.toString())
            .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.size").value(50))
        .andExpect(jsonPath("$.totalElements").value(1))
        .andExpect(jsonPath("$.hasNext").value(false))
        .andExpect(jsonPath("$.nextCursor").value(nullValue()))
        .andExpect(jsonPath("$.nextAfter").value(nullValue()))
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content[0].id").value(notificationId.toString()))
        .andExpect(jsonPath("$.content[0].content").value("test_OK"))
        .andExpect(jsonPath("$.content[0].confirmed").value(true))
        .andExpect(jsonPath("$.content[0].userId").value(userId.toString()))
        .andExpect(jsonPath("$.content[0].resourceType").value("INTEREST"))
        .andExpect(jsonPath("$.content[0].resourceId").value(resourceId.toString()))
        .andExpect(jsonPath("$.content[0].createdAt").value(now.toString()))
        .andExpect(jsonPath("$.content[0].updatedAt").value(now.toString()));
    }

    @Test
    @DisplayName("알림 목록 조회 X - 400 잘못된 요청 (limit <= 0)")
    void findAllNotificationByUserId_ERR_400() throws Exception { // HandlerMethodValidationException, ConstraintViolationException

        mockMvc.perform(get("/notifications")
                .param("limit", "0")
                .header("Monew-Request-User-ID", userId.toString())
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isBadRequest());
    }


    @Test
    @DisplayName("알림 목록 조회 X - 500 서버 내부 오류")
    void findAllNotificationByUserId_ERR_500() throws Exception {

        given(notiService.findAllByUserId(userId, null, null, limit))
            .willThrow(new RuntimeException("알림 목록 조회 XXXX - 500 서버 내부 오류"));

        mockMvc.perform(get("/notifications")
                .param("limit", String.valueOf(limit))
                .header("Monew-Request-User-ID", userId.toString())
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("전체 알림 확인 O - 200 전체 알림 확인 성공")
    void allCheckNotification_OK() throws Exception {

        willDoNothing()
            .given(notiService)
            .allCheckNotification(userId);

        mockMvc.perform(patch("/api/notifications")
                .header("Monew-Request-User-ID", userId.toString())
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());

        verify(notiService).allCheckNotification(userId);
    }

    @Test
    @DisplayName("전체 알림 확인 X - 400 잘못된 요청 (입력값 검증 실패)")
    void allCheckNotification_ERR_400() throws Exception {

        mockMvc.perform(patch("/api/notifications")
//                .header("Monew-Request-User-ID", userId.toString()) 헤더 누락
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isBadRequest());  // @Import(CommonExceptionHandler.class) + @Validated + MissingRequestHeaderException
    }

    @Test
    @DisplayName("전체 알림 확인 X - 404 사용자 정보 없음")
    void allCheckNotification_ERR_404() throws Exception {

        doThrow(new CustomException(
            ErrorCode.USER_NOT_EXIST,
            null
        )).when(notiService).allCheckNotification(userId);

        mockMvc.perform(patch("/api/notifications")
                .header("Monew-Request-User-ID", userId.toString())
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code")
                .value(ErrorCode.USER_NOT_EXIST.name()))
            .andExpect(jsonPath("$.executionType")
                .value("CustomException"));
    }

    @Test
    @DisplayName("전체 알림 확인 X - 500 서버 내부 오류")
    void allCheckNotification_ERR_500() throws Exception {

        doThrow(new RuntimeException("unexpected error"))
            .when(notiService)
            .allCheckNotification(userId);

        // when & then
        mockMvc.perform(patch("/api/notifications")
                .header("Monew-Request-User-ID", userId.toString())
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.code")
                .value(ErrorCode.INTERNAL_SERVER_ERROR.name()))
            .andExpect(jsonPath("$.executionType")
                .value("RuntimeException"));
    }

    @Test
    @DisplayName("알림 확인 O - 200 알림 확인 성공")
    void checkNotification_OK() throws Exception {

        doNothing()
            .when(notiService)
            .checkNotification(notificationId, userId);

        mockMvc.perform(patch("/notifications/{notificationId}", notificationId)
                .header("Monew-Request-User-ID", userId.toString())
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());

        verify(notiService)
            .checkNotification(notificationId, userId);
    }

    @Test
    @DisplayName("알림 확인 X - 400 잘못된 요청 (입력값 검증 실패) -  헤더 누락")
    void checkNotification_ERR_400() throws Exception {

        mockMvc.perform(patch("/notifications/{notificationId}", notificationId)
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
            .andExpect(jsonPath("$.executionType")
                .value("MissingRequestHeaderException"));
    }

    @Test
    @DisplayName("알림 확인 X - 404 사용자 정보 없음")
    void checkNotification_ERR_404() throws Exception {

        doThrow(new CustomException(
            ErrorCode.NOTIFICATION_NOT_EXIST,
            null
        )).when(notiService)
            .checkNotification(notificationId, userId);

        mockMvc.perform(patch("/notifications/{notificationId}", notificationId)
                .header("Monew-Request-User-ID", userId.toString())
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code")
                .value(ErrorCode.NOTIFICATION_NOT_EXIST.name()))
            .andExpect(jsonPath("$.executionType")
                .value("CustomException"));
    }

    @Test
    @DisplayName("알림 확인 X - 500 서버 내부 오류")
    void checkNotification_ERR_500() throws Exception {

        doThrow(new RuntimeException("unexpected error"))
            .when(notiService)
            .checkNotification(notificationId, userId);

        mockMvc.perform(patch("/notifications/{notificationId}", notificationId)
                .header("Monew-Request-User-ID", userId.toString())
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.code")
                .value(ErrorCode.INTERNAL_SERVER_ERROR.name()))
            .andExpect(jsonPath("$.executionType")
                .value("RuntimeException"));
    }

}