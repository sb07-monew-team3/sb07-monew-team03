package com.example.monew.domain.notification.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import com.example.monew.domain.interest.entity.Interest;
import com.example.monew.domain.interest.service.SubscriptionServiceImpl;
import com.example.monew.domain.notification.dto.NotificationDto;
import com.example.monew.domain.notification.entity.Notifications;
import com.example.monew.domain.notification.entity.ResourceType;
import com.example.monew.domain.notification.repository.NotificationRepository;
import com.example.monew.domain.notification.response.CursorResponse;
import com.example.monew.domain.user.dto.UserDto;
import com.example.monew.domain.user.entity.User;
import com.example.monew.domain.user.repository.UserRepository;
import com.example.monew.domain.user.util.TestFixture;
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
import org.springframework.test.util.ReflectionTestUtils;
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

//    @InjectMocks
//    private SubscriptionServiceImpl subscriptionService;

    @Mock
    private SubscriptionServiceImpl subscriptionService; // ⭐ 반드시 필요


//    private UUID userId;
//    private UUID notificationId;
    private final int limit = 50;
    private NotificationDto notificationDto;

    private final TestFixture testFixture = new TestFixture();
    private User user;
    private UserDto userDto;

    @BeforeEach
    void setUp() {

        user = testFixture.userFactory();
        userDto = new UserDto(user.getId(),user.getEmail(),user.getNickName(),user.getCreatedAt());

//        userId = UUID.randomUUID();
//        notificationId = UUID.randomUUID();
//        notificationDto = new NotificationDto(userId, )
    }

    @Test
    @DisplayName("case - 알림 목록 조회 (cursor 기반)")
    void findAllByUserId() {
        // given
        String cursor = "0";
        Instant createdAt = Instant.now();
        int limit = 50;

        UUID notificationId = UUID.randomUUID();

        Notifications notifications = new Notifications(
            user,
            "testContent",
            ResourceType.INTEREST,
            UUID.randomUUID(),
            false,
            createdAt
        );

        ReflectionTestUtils.setField(notifications, "id", notificationId);

        Slice<Notifications> slice = new SliceImpl<>(
            List.of(notifications),
            PageRequest.of(0, limit),
            false
        );

        given(notiRepository.findAllByUserId(
            eq(user.getId()),
            any(Instant.class),
            any(Pageable.class)
        )).willReturn(slice);

        // when
        CursorResponse<NotificationDto> result =
            notiService.findAllByUserId(user.getId(), cursor, createdAt, limit);

        // then
        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).id()).isEqualTo(notificationId);
    }

    @Test
    @DisplayName("case - user의 전체 알림을 한번에 확인")
    void allCheckNotification() {

        UUID userId = UUID.randomUUID();

        ReflectionTestUtils.setField(user, "id", userId);
        ReflectionTestUtils.setField(user, "createdAt", Instant.now());

        Notifications noti1 = spy(new Notifications(
            user,
            "content1",
            ResourceType.INTEREST,
            UUID.randomUUID(),
            false,
            Instant.now()
        ));

        Notifications noti2 = spy(new Notifications(
            user,
            "content2",
            ResourceType.INTEREST,
            UUID.randomUUID(),
            false,
            Instant.now()
        ));

        given(notiRepository.findAllByUserId(user.getId()))
            .willReturn(List.of(noti1, noti2));

        // when
        notiService.allCheckNotification(user.getId());

        // then
        verify(notiRepository).findAllByUserId(user.getId());
        verify(noti1).checkNotificationRead(user.getId());
        verify(noti2).checkNotificationRead(user.getId());
    }

    @Test
    @DisplayName("case - user의 특정 알림 확인")
    void checkNotification() {
        // given
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();

        User user = new User(
            "test@test.com",
            "tester",
            "password",
            null
        );

        ReflectionTestUtils.setField(user, "id", userId);
        ReflectionTestUtils.setField(user, "createdAt", Instant.now());

        Notifications notifications = spy(new Notifications(
            user,
            "testContent",
            ResourceType.INTEREST,
            UUID.randomUUID(),
            false,
            Instant.now()
        ));

        ReflectionTestUtils.setField(notifications, "id", notificationId);
        ReflectionTestUtils.setField(notifications, "createdAt", Instant.now());

        given(notiRepository.findByIdAndUserId(notificationId, userId))
            .willReturn(Optional.of(notifications));

        // when
        notiService.checkNotification(notificationId, userId);

        // then
        verify(notiRepository).findByIdAndUserId(notificationId, userId);
        verify(notifications).checkNotificationRead(userId);
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
    @DisplayName("구독 관심사 관련 새 기사 등록시 알림 생성 - OK")
    void createInterestAlarm_OK() {
        // given
        UUID interestId = UUID.randomUUID();
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();

        Interest interest = mock(Interest.class);
        given(interest.getId()).willReturn(interestId);
        given(interest.getName()).willReturn("스프링");

        Map<Interest, Integer> interestMap = Map.of(
            interest, 3
        );

        given(subscriptionService.getSubscribedInterestIds(interestId))
            .willReturn(List.of(userId1, userId2));

        User user1 = new User("u1@test.com", "u1", "pw", null);
        User user2 = new User("u2@test.com", "u2", "pw", null);

        ReflectionTestUtils.setField(user1, "id", userId1);
        ReflectionTestUtils.setField(user2, "id", userId2);

        given(userRepository.findById(userId1))
            .willReturn(Optional.of(user1));
        given(userRepository.findById(userId2))
            .willReturn(Optional.of(user2));

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
                .isIn(userId1, userId2);
            assertThat(notification.getResourceType())
                .isEqualTo(ResourceType.INTEREST);
            assertThat(notification.getResourceId())
                .isEqualTo(interestId);
            assertThat(notification.getContent())
                .contains("[스프링]")
                .contains("3건 등록되었습니다");
            assertThat(notification.isRead()).isFalse();
        });

        verify(subscriptionService)
            .getSubscribedInterestIds(interestId);

        verify(userRepository).findById(userId1);
        verify(userRepository).findById(userId2);
    }

}