package com.example.monew.domain.interest.unit.service;

import com.example.monew.domain.interest.dto.InterestDto;
import com.example.monew.domain.interest.dto.InterestRegisterRequest;
import com.example.monew.domain.interest.entity.Interest;
import com.example.monew.domain.interest.mapper.InterestMapper;
import com.example.monew.domain.interest.repository.InterestRepository;
import com.example.monew.domain.interest.repository.KeywordRepository;
import com.example.monew.domain.interest.service.InterestService;
import org.junit.jupiter.api.DisplayName;
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
    private InterestService interestService;

    @Test
    @DisplayName("관심사와 키워드를 등록한다")
    void create_interestOrKeyword() {

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
}
