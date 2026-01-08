package com.example.monew.domain.interest.unit.service;

import com.example.monew.domain.interest.dto.InterestDto;
import com.example.monew.domain.interest.dto.InterestRegisterRequest;
import com.example.monew.domain.interest.entity.Interest;
import com.example.monew.domain.interest.mapper.InterestMapper;
import com.example.monew.domain.interest.repository.InterestRepository;
import com.example.monew.domain.interest.repository.KeywordRepository;
import com.example.monew.domain.interest.service.InterestServiceImpl;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class InterestServiceTest {

    @Mock
    private InterestRepository interestRepository;

    @Mock
    private KeywordRepository keywordRepository;

    @Mock
    private InterestMapper interestMapper;

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
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("관심사 이름은 필수입니다.");
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
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("유사한 이름의 관심사가 이미 존재합니다.");

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
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("유사한 이름의 관심사가 이미 존재합니다.");

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
}
