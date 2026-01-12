package com.example.monew.domain.interest.unit.service;

import com.example.monew.domain.interest.dto.SubscriptionDto;
import com.example.monew.domain.interest.entity.Interest;
import com.example.monew.domain.interest.entity.Keyword;
import com.example.monew.domain.interest.entity.Subscription;
import com.example.monew.domain.interest.mapper.SubscriptionMapper;
import com.example.monew.domain.interest.repository.InterestRepository;
import com.example.monew.domain.interest.repository.KeywordRepository;
import com.example.monew.domain.interest.repository.SubscriptionRepository;
import com.example.monew.domain.interest.service.SubscriptionServiceImpl;
import com.example.monew.domain.user.entity.User;
import com.example.monew.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class subscriptionServiceTest {

    @Mock
    private InterestRepository interestRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private KeywordRepository keywordRepository;

    @Mock
    private SubscriptionMapper subscriptionMapper;

    @InjectMocks
    private SubscriptionServiceImpl subscriptionService;

    @Nested
    @DisplayName("관심사 구독")
    class subscription {

        @Test
        @DisplayName("사용자는 관심사를 구독할 수 있다")
        void subscribe_success() {

            // given
            UUID userId = UUID.randomUUID();
            UUID interestId = UUID.randomUUID();

            User user = new User("test@test.com", "아토", "Z1x2c3v4!", null);
            Interest interest = new Interest("동물");
            ReflectionTestUtils.setField(interest, "id", interestId);

            List<Keyword> keywords = List.of(
                    new Keyword("강아지", interest),
                    new Keyword("고양이", interest)
            );

            Subscription subscription = new Subscription(interest, user);
            ReflectionTestUtils.setField(subscription, "id", UUID.randomUUID());
            ReflectionTestUtils.setField(subscription, "createdAt", Instant.now());

            when(userRepository.findById(userId))
                    .thenReturn(Optional.of(user));

            when(interestRepository.findById(interestId))
                    .thenReturn(Optional.of(interest));

            when(subscriptionRepository.save(any(Subscription.class)))
                    .thenReturn(subscription);

            when(keywordRepository.findByInterest(interest))
                    .thenReturn(keywords);

            when(subscriptionRepository.countByInterestId(interestId))
                    .thenReturn(1L);

            SubscriptionDto subscriptionDto = new SubscriptionDto(
                    subscription.getId(),
                    interestId,
                    "동물",
                    List.of("강아지", "고양이"),
                    1L,
                    subscription.getCreatedAt()
            );

            when(subscriptionMapper.toDto(any(Subscription.class), any(List.class), anyLong()))
                    .thenReturn(subscriptionDto);

            // when
            SubscriptionDto subscribe = subscriptionService.subscribe(interestId, userId);

            // then
            assertThat(subscribe.interestId()).isEqualTo(interestId);
            assertThat(subscribe.interestName()).isEqualTo("동물");
            assertThat(subscribe.interestSubscriberCount()).isEqualTo(1L);

            verify(subscriptionRepository).save(any(Subscription.class));
            verify(keywordRepository).findByInterest(interest);
            verify(subscriptionRepository).countByInterestId(interestId);
        }

        @Test
        @DisplayName("사용자는 관심사 구독을 취소할 수 있다")
        void unsubscribe_Success() {
            // given
            UUID userId = UUID.randomUUID();
            UUID interestId = UUID.randomUUID();

            User user = new User("test@test.com", "아토", "Z1x2c3v4!", null);
            Interest interest = new Interest("동물");
            Subscription subscription = new Subscription(interest, user);

            when(subscriptionRepository.findSubscription(userId, interestId))
                    .thenReturn(Optional.of(subscription));

            // when
            subscriptionService.unsubscribe(interestId, userId);

            // then
            verify(subscriptionRepository).delete(subscription);

        }
    }
}
