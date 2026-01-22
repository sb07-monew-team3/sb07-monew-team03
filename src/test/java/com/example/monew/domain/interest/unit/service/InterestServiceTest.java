package com.example.monew.domain.interest.unit.service;

import com.example.monew.domain.activity.service.MongoDbService;
import com.example.monew.domain.interest.dto.CursorPageResponseInterestDto;
import com.example.monew.domain.interest.dto.InterestDto;
import com.example.monew.domain.interest.dto.InterestRegisterRequest;
import com.example.monew.domain.interest.dto.InterestUpdateRequest;
import com.example.monew.domain.interest.entity.Interest;
import com.example.monew.domain.interest.entity.Keyword;
import com.example.monew.domain.interest.entity.Subscription;
import com.example.monew.domain.interest.mapper.InterestMapper;
import com.example.monew.domain.interest.repository.InterestRepository;
import com.example.monew.domain.interest.repository.KeywordRepository;
import com.example.monew.domain.interest.repository.SubscriptionRepository;
import com.example.monew.domain.interest.service.InterestServiceImpl;
import com.example.monew.domain.interest.service.SubscriptionServiceImpl;
import com.example.monew.domain.user.entity.User;
import com.example.monew.domain.user.repository.UserRepository;
import com.example.monew.global.exception.domain.interest.InterestDuplicateNameException;
import com.example.monew.global.exception.domain.interest.InterestNotExistException;
import com.mongodb.client.MongoClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InterestServiceTest {

    @Mock
    private InterestRepository interestRepository;

    @Mock
    private KeywordRepository keywordRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private InterestMapper interestMapper;

    @Mock
    private MongoDbService mongoDbService;


    @InjectMocks
    private InterestServiceImpl interestService;

    @Nested
    @DisplayName("관심사 등록")
    class InterestRegister {

        @Test
        @DisplayName("관심사와 키워드를 등록한다")
        void create_interestOrKeyword_success() {

            // given
            InterestRegisterRequest request = new InterestRegisterRequest(
                    "축구",
                    List.of("손흥민", "인테르")
            );

            Interest interest = new Interest("축구");
            ReflectionTestUtils.setField(interest, "id", UUID.randomUUID());
            ReflectionTestUtils.setField(interest, "createdAt", Instant.now());

            when(interestRepository.save(any(Interest.class)))
                    .thenReturn(interest);

            InterestDto interestDto = new InterestDto(
                    interest.getId(),
                    "축구",
                    List.of("손흥민", "인테르"),
                    0L,
                    false
            );

            when(interestMapper.toDto(any(Interest.class), any(List.class)))
                    .thenReturn(interestDto);

            // when
            InterestDto result = interestService.create(request);

            // then
            assertThat(result.id()).isNotNull();
            assertThat(result.name()).isEqualTo("축구");
            assertThat(result.keywords()).containsExactly("손흥민", "인테르");
            assertThat(result.subscriberCount()).isZero();
            assertThat(result.subscribedByMe()).isFalse();
        }

        @Test
        @DisplayName("관심사 이름이 없으면 예외 발생")
        void create_nullName_throwsException() {

            // given
            InterestRegisterRequest request = new InterestRegisterRequest(
                    null,
                    List.of("아토", "최고")
            );

            // when && then
            assertThatThrownBy(() -> interestService.create(request))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("관심사 이름이 빈 문자열이면 예외 발생")
        void create_blankName_throwsException() {

            // given
            InterestRegisterRequest request = new InterestRegisterRequest(
                    "",
                    List.of("아토", "최고")
            );

            // when && then
            assertThatThrownBy(() -> interestService.create(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("관심사 이름은 필수입니다.");
        }

        @Test
        @DisplayName("관심사 이름이 완전 동일하면 중복 예외 발생")
        void create_duplicateName_throwsException() {

            // given
            InterestRegisterRequest request = new InterestRegisterRequest(
                    "축구",
                    List.of("손흥민", "인테르")
            );

            when(interestRepository.existsByName(request.name()))
                    .thenReturn(true);


            // when && then
            assertThatThrownBy(() -> interestService.create(request))
                    .isInstanceOf(InterestDuplicateNameException.class);

        }

        @Test
        @DisplayName("80% 유사한 관심사 이름이 있으면 예외 발생")
        void create_similarName_throwsException() {

            // given
            InterestRegisterRequest request = new InterestRegisterRequest(
                    "유사한관심사",
                    List.of("아토")
            );

            when(interestRepository.findAllNames())
                    .thenReturn(List.of("유사한관심서"));


            // when && then
            assertThatThrownBy(() -> interestService.create(request))
                    .isInstanceOf(InterestDuplicateNameException.class);
        }

        @Test
        @DisplayName("80% 미만 유사한 관심사 이름이 있으면 생성")
        void create_similarName_success() {
            // given
            InterestRegisterRequest request = new InterestRegisterRequest(
                    "아토",
                    List.of("산책", "강아지")
            );

            when(interestRepository.findAllNames())
                    .thenReturn(List.of("아투"));

            Interest interest = new Interest("아토");
            ReflectionTestUtils.setField(interest, "id", UUID.randomUUID());
            ReflectionTestUtils.setField(interest, "createdAt", Instant.now());

            when(interestRepository.save(any(Interest.class)))
                    .thenReturn(interest);

            InterestDto interestDto = new InterestDto(
                    interest.getId(),
                    "아토",
                    List.of("산책", "강아지"),
                    0L,
                    false
            );

            when(interestMapper.toDto(any(Interest.class), any(List.class)))
                    .thenReturn(interestDto);

            // when
            InterestDto result = interestService.create(request);


            // then
            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("관심사 키워드 수정")
    class UpdateInterestKeywords {

        @Test
        @DisplayName("키워드를 수정할 수 있다")
        void update_keywords_success() {

            // given
            Interest interest = new Interest("축구");
            ReflectionTestUtils.setField(interest, "id", UUID.randomUUID());

            List<String> newKeywords = List.of("손흥민", "인테르", "챔스");

            InterestUpdateRequest request = new InterestUpdateRequest(newKeywords);

            when(interestRepository.findById(interest.getId()))
                    .thenReturn(Optional.of(interest));
            doNothing().when(keywordRepository).deleteByInterestId(interest.getId());
            doNothing().when(mongoDbService).updateSubscription(any());
            when(subscriptionRepository.countByInterestId(interest.getId()))
                    .thenReturn(1L);

            InterestDto interestDto = new InterestDto(
                    interest.getId(),
                    "축구",
                    newKeywords,
                    1L,
                    null
            );

            when(interestMapper.toDto(
                    any(Interest.class),
                    any(List.class),
                    anyLong(),
                    any()
            )).thenReturn(interestDto);

            // when
            InterestDto result = interestService.update(interest.getId(), request);

            // then
            assertThat(result.id()).isNotNull();
            assertThat(result.keywords()).containsExactly("손흥민", "인테르", "챔스");

            verify(keywordRepository).deleteByInterestId(interest.getId());
            verify(keywordRepository).saveAll(anyList());
            verify(subscriptionRepository).countByInterestId(interest.getId());
        }

        @Test
        @DisplayName("존재하지 않는 관심사로 수정 시 예외 발생")
        void update_notFoundInterest_throwsException() {

            // given
            UUID interestId = UUID.randomUUID();
            List<String> keywords = List.of("산책", "날씨");
            InterestUpdateRequest request = new InterestUpdateRequest(keywords);

            when(interestRepository.findById(interestId))
                    .thenReturn(Optional.empty());

            // when && then
            assertThatThrownBy(() -> interestService.update(interestId, request))
                    .isInstanceOf(InterestNotExistException.class);
        }
    }

    @Nested
    @DisplayName("관심사 삭제")
    class DeleteInterest {

        @Test
        @DisplayName("관심사를 삭제할 수 있다")
        void delete_interest_success() {

            // given
            Interest interest = new Interest("동물");
            ReflectionTestUtils.setField(interest, "id", UUID.randomUUID());
            ReflectionTestUtils.setField(interest, "createdAt", Instant.now());

            when(interestRepository.findById(interest.getId()))
                    .thenReturn(Optional.of(interest));
            doNothing().when(mongoDbService).updateWhenSubscriptionDelete(any(),any());
            // when
            interestService.delete(interest.getId());

            // then
            verify(interestRepository).findById(interest.getId());
            verify(interestRepository).delete(interest);
        }

        @Test
        @DisplayName("존재하지 않는 관심사 삭제 시 예외 발생")
        void delete_notFoundInterest_throwsException() {

            // given
            UUID interestId = UUID.randomUUID();

            when(interestRepository.findById(interestId))
                    .thenReturn(Optional.empty());

            // when && then
            assertThatThrownBy(() -> interestService.delete(interestId))
                    .isInstanceOf(InterestNotExistException.class);
        }
    }

    @Nested
    @DisplayName("관심사 목록 조회")
    class searchInterest {

        @Test
        @DisplayName("관심사 이름으로 조회할 수 있다")
        void search_interestName_success() {

            // given
            UUID userId = UUID.randomUUID();
            String searchKeyword = "동물";
            int limit = 10;

            Interest interest = new Interest("동물 농장");
            ReflectionTestUtils.setField(interest, "id", UUID.randomUUID());
            ReflectionTestUtils.setField(interest, "createdAt", Instant.now());

            List<String> keywordString = List.of("강아지", "고양이");
            List<Keyword> keywords = List.of(
                    new Keyword("강아지", interest),
                    new Keyword("고양이", interest));

            when(interestRepository.searchByInterestOrKeyword(
                    "동물", "name", "DESC", null, null, limit))
                    .thenReturn(List.of(interest));

            when(keywordRepository.findByInterest(interest))
                    .thenReturn(keywords);

            when(subscriptionRepository.countByInterestId(interest.getId()))
                    .thenReturn(0L);

            when(subscriptionRepository.isSubscribed(userId, interest.getId()))
                    .thenReturn(false);

            InterestDto interestDto = new InterestDto(
                    interest.getId(),
                    "동물 농장",
                    keywordString,
                    0L,
                    false
            );

            when(interestMapper.toDto(
                    any(Interest.class),
                    any(List.class),
                    anyLong(),
                    anyBoolean()
            )).thenReturn(interestDto);

            // when
            CursorPageResponseInterestDto result = interestService.search(
                    searchKeyword, userId, "name", "DESC", null, null, limit);

            // then
            assertThat(result.content()).hasSize(1);
            assertThat(result.content().get(0).name()).isEqualTo("동물 농장");
            assertThat(result.content().get(0).keywords()).containsExactly("강아지", "고양이");
            assertThat(result.nextCursor()).isNull();
            assertThat(result.nextAfter()).isNull();

            verify(keywordRepository).findByInterest(interest);
            verify(subscriptionRepository).countByInterestId(interest.getId());
            verify(subscriptionRepository).isSubscribed(userId, interest.getId());
        }

        @Test
        @DisplayName("키워드 이름으로 조회할 수 있다")
        void search_keywordName_success() {

            // given
            UUID userId = UUID.randomUUID();
            String searchKeyword = "축구";
            int limit = 10;

            Interest interest = new Interest("스포츠");
            ReflectionTestUtils.setField(interest, "id", UUID.randomUUID());
            ReflectionTestUtils.setField(interest, "createdAt", Instant.now());

            List<String> keywordString = List.of("축구", "야구");
            List<Keyword> keywords = List.of(
                    new Keyword("축구", interest),
                    new Keyword("야구", interest));

            when(interestRepository.searchByInterestOrKeyword(
                    "축구", "name", "DESC", null, null, limit))
                    .thenReturn(List.of(interest));

            when(keywordRepository.findByInterest(interest))
                    .thenReturn(keywords);

            when(subscriptionRepository.countByInterestId(interest.getId()))
                    .thenReturn(0L);

            when(subscriptionRepository.isSubscribed(userId, interest.getId()))
                    .thenReturn(false);

            InterestDto interestDto = new InterestDto(
                    interest.getId(),
                    "스포츠",
                    keywordString,
                    0L,
                    false
            );

            when(interestMapper.toDto(
                    any(Interest.class),
                    any(List.class),
                    anyLong(),
                    anyBoolean()
            )).thenReturn(interestDto);

            // when
            CursorPageResponseInterestDto result = interestService.search(
                    searchKeyword, userId, "name", "DESC", null, null, limit);


            // then
            assertThat(result.content()).hasSize(1);
            assertThat(result.content().get(0).name()).isEqualTo("스포츠");
            assertThat(result.content().get(0).keywords()).containsExactly("축구", "야구");
            assertThat(result.hasNext()).isFalse();
        }

        @Test
        @DisplayName("조회 결과가 없으면 빈 리스트 반환한다")
        void search_emptyResult_returnEmptyList() {

            // given
            UUID userId = UUID.randomUUID();
            String keyword = "동물";
            int limit = 10;

            when(interestRepository.searchByInterestOrKeyword(
                    keyword, "name", "DESC", null, null, limit))
                    .thenReturn(List.of());

            // when
            CursorPageResponseInterestDto search = interestService.search(
                    keyword, userId, "name", "DESC", null, null, limit);

            // then
            assertThat(search.content()).isEmpty();
            assertThat(search.content()).hasSize(0);
            assertThat(search.hasNext()).isFalse();
        }
        
        @Test
        @DisplayName("name 기준 오름차순 정렬한다")
        void search_orderByNameAsc_sorted() {

            // given
            UUID userId = UUID.randomUUID();
            int limit = 10;

            Interest interest = new Interest("가나다");
            ReflectionTestUtils.setField(interest, "id", UUID.randomUUID());
            ReflectionTestUtils.setField(interest, "createdAt", Instant.now());

            Interest interest2 = new Interest("라마바");
            ReflectionTestUtils.setField(interest2, "id", UUID.randomUUID());
            ReflectionTestUtils.setField(interest2, "createdAt", Instant.now());

            when(interestRepository.searchByInterestOrKeyword(
                    null, "name", "ASC", null, null, limit))
                    .thenReturn(List.of(interest, interest2));

            when(keywordRepository.findByInterest(any(Interest.class)))
                    .thenReturn(List.of());

            when(subscriptionRepository.countByInterestId(any(UUID.class)))
                    .thenReturn(0L);

            when(subscriptionRepository.isSubscribed(any(UUID.class), any(UUID.class)))
                    .thenReturn(false);

            when(interestMapper.toDto(eq(interest), anyList(), anyLong(), anyBoolean()))
                    .thenReturn(new InterestDto(
                            interest.getId(), interest.getName(), List.of(), 0L, false));

            when(interestMapper.toDto(eq(interest2), anyList(), anyLong(), anyBoolean()))
                    .thenReturn(new InterestDto(
                            interest2.getId(), interest2.getName(), List.of(), 0L, false));

            // when
            CursorPageResponseInterestDto search = interestService.search(
                    null, userId, "name", "ASC", null, null, limit);

            // then
            assertThat(search.content()).hasSize(2);
            assertThat(search.content().get(0).name()).isEqualTo("가나다");
            assertThat(search.content().get(1).name()).isEqualTo("라마바");
            assertThat(search.hasNext()).isFalse();
        }

        @Test
        @DisplayName("다음 페이지가 있으면 hasNext와 커서를 반환한다")
        void search_hasNextTrue_returnNextCursor() {

            // given
            UUID userId = UUID.randomUUID();
            int limit = 2;

            Interest interest = new Interest("축구");
            ReflectionTestUtils.setField(interest, "id", UUID.randomUUID());
            ReflectionTestUtils.setField(interest, "createdAt", Instant.now());

            Interest interest2 = new Interest("야구");
            ReflectionTestUtils.setField(interest2, "id", UUID.randomUUID());
            ReflectionTestUtils.setField(interest2, "createdAt", Instant.now());

            Interest interest3 = new Interest("배구");
            ReflectionTestUtils.setField(interest3, "id", UUID.randomUUID());
            ReflectionTestUtils.setField(interest3, "createdAt", Instant.now());

            when(interestRepository.searchByInterestOrKeyword(
                    null, "name", "ASC", null, null, limit))
                    .thenReturn(List.of(interest, interest2, interest3));

            when(keywordRepository.findByInterest(any(Interest.class)))
                    .thenReturn(List.of());

            when(subscriptionRepository.countByInterestId(any(UUID.class)))
                    .thenReturn(0L);

            when(subscriptionRepository.isSubscribed(any(UUID.class), any(UUID.class)))
                    .thenReturn(false);

            when(interestMapper.toDto(eq(interest), anyList(), anyLong(), anyBoolean()))
                    .thenReturn(new InterestDto(
                            interest.getId(), interest.getName(), List.of(), 0L, false));

            when(interestMapper.toDto(eq(interest2), anyList(), anyLong(), anyBoolean()))
                    .thenReturn(new InterestDto(
                            interest2.getId(), interest2.getName(), List.of(), 0L, false));

            // when
            CursorPageResponseInterestDto search = interestService.search(
                    null, userId, "name", "ASC", null, null, limit);

            // then
            assertThat(search.hasNext()).isTrue();
            assertThat(search.nextCursor()).isNotNull();
            assertThat(search.nextAfter()).isNotNull();
            assertThat(search.content()).hasSize(2);
        }
    }
}
