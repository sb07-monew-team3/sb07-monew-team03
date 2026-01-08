package com.example.monew.domain.interest.integration.service;

import com.example.monew.domain.interest.dto.InterestDto;
import com.example.monew.domain.interest.dto.InterestRegisterRequest;
import com.example.monew.domain.interest.entity.Interest;
import com.example.monew.domain.interest.entity.Keyword;
import com.example.monew.domain.interest.repository.InterestRepository;
import com.example.monew.domain.interest.repository.KeywordRepository;
import com.example.monew.domain.interest.service.InterestService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class InterestServiceIntegrationTest {

    @Autowired
    private InterestService interestService;

    @Autowired
    private InterestRepository interestRepository;

    @Autowired
    private KeywordRepository keywordRepository;


    @Test
    @DisplayName("관심사 이름과 키워드가 저장된다")
    void create_saveInterestAndKeywords_success() {
        // given
        InterestRegisterRequest request = new InterestRegisterRequest(
                "강아지",
                List.of("산책", "날씨")
        );

        // when
        InterestDto result = interestService.create(request);


        // then
        assertThat(result.id()).isNotNull();
        assertThat(result.name()).isEqualTo("강아지");
        assertThat(result.keywords()).containsExactly("산책", "날씨");

        Interest interest = interestRepository.findById(result.id()).orElseThrow();
        assertThat(interest.getName()).isEqualTo("강아지");

        List<Keyword> keywords = keywordRepository.findAllByInterestId(interest.getId());
        assertThat(keywords).hasSize(2);

    }
}
