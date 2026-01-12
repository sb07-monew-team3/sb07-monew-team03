package com.example.monew.domain.notification.service;

import static org.assertj.core.api.BDDAssertions.then;
import static org.awaitility.Awaitility.given;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

import com.example.monew.domain.notification.dto.NotificationDto;
import com.example.monew.domain.notification.entity.Notifications;
import com.example.monew.domain.notification.entity.ResourceType;
import com.example.monew.domain.notification.repository.NotificationRepository;
import com.example.monew.domain.user.entity.User;
import com.example.monew.domain.user.util.NotiFactory;
import com.example.monew.domain.user.util.TestFixture;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;

@Transactional
@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService Unit Test")
class NotificationServiceUnitTest {
    @Mock
    private NotificationRepository notiRepository;

    @Mock
    private NotificationDto dto;

    @InjectMocks
    private NotificationService notiService;

    @Autowired
    NotiFactory notiFactory;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    @DisplayName("case  - 알림 목록을 조회")
    void findAllNotificationByUserId() {
    }

    @Test
    @DisplayName("case  - user의 전체 알림을 한번에 확인")
    void allCheckNotification() {
    }

    @Test
    @DisplayName("case  - user의 특정 알림 확인")
    void checkNotification() {
    }

//    ‼️통합테스트로 넘김‼️
//    @Test
//    @DisplayName("case  - 배치 처리 확인")
//    void deleteNotificationInBatch() {
//        //given
//
//
//        //when
//        noti_I.checkNotificationRead(user.getId());
//        noti_II.checkNotificationRead(user.getId());
//
//        notiRepository.saveAll(List.of(noti_I,noti_II));
//
//        notiService.deleteNotificationInBatch();
//
//        //then
//        assertThat()
//    }
}