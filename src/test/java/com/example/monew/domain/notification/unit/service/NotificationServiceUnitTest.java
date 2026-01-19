package com.example.monew.domain.notification.unit.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.example.monew.domain.interest.entity.Interest;
import com.example.monew.domain.interest.service.SubscriptionServiceImpl;
import com.example.monew.domain.notification.dto.NotificationDto;
import com.example.monew.domain.notification.entity.Notifications;
import com.example.monew.domain.notification.entity.ResourceType;
import com.example.monew.domain.notification.repository.NotificationRepository;
import com.example.monew.domain.notification.response.CursorResponse;
import com.example.monew.domain.notification.service.NotificationServiceImpl;
import com.example.monew.domain.notification.util.NotiFactory;
import com.example.monew.domain.user.entity.User;
import com.example.monew.domain.user.repository.UserRepository;
import com.example.monew.global.exception.domain.notification.NotificationNotExistException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService Unit Test")
class NotificationServiceUnitTest {
    @Mock
    private NotificationRepository notiRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NotificationServiceImpl notiService;

    @Mock
    private SubscriptionServiceImpl subscriptionService; // ⭐ 반드시 필요

    private final int limit = 50;
    private NotificationDto notificationDto;

    private final NotiFactory notiFactory = new NotiFactory();

    private User mockUserI;
    private User mockUserII;
    Notifications mockNotiI;
    Notifications mockNotiII;


    @BeforeEach
    void setUp() {

        mockUserI = notiFactory.mockUser("nick1");
        mockUserII = notiFactory.mockUser("nick2");
        mockNotiI = notiFactory.mockNoti(mockUserI);
        mockNotiII = notiFactory.mockNoti(mockUserI);
    }

    @Test
    @DisplayName("case - 알림 목록 조회 (cursor 기반)")
    void findAllByUserId() {
        // given
        Slice<Notifications> slice = new SliceImpl<>(
            List.of(mockNotiI),
            PageRequest.of(0, limit),
            false
        );

        given(notiRepository.findAllByUserId(eq(mockUserI.getId()), any(Instant.class), any(Pageable.class)))
            .willReturn(slice);

        // when
        String cursor = "0";
        Instant createdAt = Instant.now();
        int limit = 50;

        CursorResponse<NotificationDto> result =
            notiService.findAllByUserId(mockUserI.getId(), cursor, createdAt, limit);

        // then
        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).id()).isEqualTo(mockNotiI.getId());
    }

    @Test
    @DisplayName("case - user의 전체 알림을 한번에 확인 (상태 검증)")
    void allCheckNotification() {

        given(notiRepository.findAllByUserId(mockUserI.getId()))
            .willReturn(List.of(mockNotiI, mockNotiII));

        notiService.allCheckNotification(mockUserI.getId());

        verify(notiRepository).findAllByUserId(mockUserI.getId());

        assertTrue(mockNotiI.isRead());
        assertTrue(mockNotiII.isRead());
    }

    @Test
    @DisplayName("case - user의 특정 알림 확인 (상태 검증)")
    void checkNotification() {

        given(notiRepository.findByIdAndUserId(mockNotiI.getId(), mockUserI.getId()))
            .willReturn(Optional.of(mockNotiI));

        notiService.checkNotification(mockNotiI.getId(), mockUserI.getId());

        verify(notiRepository).findByIdAndUserId(mockNotiI.getId(), mockUserI.getId());

        assertTrue(mockNotiI.isRead());
    }


    @Test
    @DisplayName("case - 알림이 존재하지 않으면 예외 발생")
    void checkNotification_notFound() {
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();

        given(notiRepository.findByIdAndUserId(notificationId, userId))
            .willReturn(Optional.empty());

        assertThatThrownBy(() ->
            notiService.checkNotification(notificationId, userId)
        ).isInstanceOf(NotificationNotExistException.class);
    }

    @Test
    @DisplayName("case - 내가 작성한 댓글에 좋아요가 눌리면 알림 생성")
    void notifyCommentLiked() {
        // given
        UUID commentId = UUID.randomUUID();
        String actorNickname = "liker";

        given(userRepository.findById(mockUserI.getId()))
            .willReturn(Optional.of(mockUserI));

        notiService.notifyCommentLiked(mockUserI.getId(), actorNickname, commentId);

        ArgumentCaptor<Notifications> captor = ArgumentCaptor.forClass(Notifications.class);
        verify(userRepository).findById(mockUserI.getId());
        verify(notiRepository).save(captor.capture());

        Notifications savedNotification = captor.getValue();

        assertAll(
            () -> assertEquals(mockUserI, savedNotification.getUser()),
            () -> assertEquals( "[" + actorNickname + "]님이 나의 댓글을 좋아합니다.", savedNotification.getContent()),
            () -> assertEquals(ResourceType.COMMENT, savedNotification.getResourceType()),
            () -> assertEquals(commentId, savedNotification.getResourceId()),
            () -> assertFalse(savedNotification.isRead())
        );
    }

    @Test
    @DisplayName("case - 구독 관심사 관련 새 기사 등록시 알림 생성 - OK")
    void createInterestAlarm_OK() {

        UUID interestId = UUID.randomUUID();

        Interest interest = mock(Interest.class);
        given(interest.getId()).willReturn(interestId);
        given(interest.getName()).willReturn("스프링");

        Map<Interest, Integer> interestMap = Map.of(
            interest, 3
        );

        given(subscriptionService.getSubscribedInterestIds(interestId))
            .willReturn(List.of(mockUserI.getId(), mockUserII.getId()));

        given(userRepository.findById(mockUserI.getId()))
            .willReturn(Optional.of(mockUserI));
        given(userRepository.findById(mockUserII.getId()))
            .willReturn(Optional.of(mockUserII));

        // when
        notiService.createInterestAlarm(interestMap);

        // then
        ArgumentCaptor<Notifications> captor =
            ArgumentCaptor.forClass(Notifications.class);

        verify(notiRepository, times(2))
            .save(captor.capture());

        List<Notifications> savedNotifications = captor.getAllValues();
        assertThat(savedNotifications).hasSize(2);

        savedNotifications.forEach(notification -> {
            assertThat(notification.getUser().getId())
                .isIn(mockUserI.getId(), mockUserII.getId());
            assertThat(notification.getResourceType())
                .isEqualTo(ResourceType.INTEREST);
            assertThat(notification.getResourceId())
                .isEqualTo(interestId);
            assertThat(notification.getContent())
                .contains("[스프링]")
                .contains("3건 등록되었습니다");
            assertThat(notification.isRead()).isFalse();
        });

        verify(subscriptionService).getSubscribedInterestIds(interestId);
        verify(userRepository).findById(mockUserI.getId());
        verify(userRepository).findById(mockUserII.getId());
    }
}