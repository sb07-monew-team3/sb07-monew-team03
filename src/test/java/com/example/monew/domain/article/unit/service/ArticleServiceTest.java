package com.example.monew.domain.article.unit.service;

import com.example.monew.domain.article.client.naver.NaverNewsClient;
import com.example.monew.domain.article.client.naver.NaverNewsResponse;
import com.example.monew.domain.article.entity.Article;
import com.example.monew.domain.article.mapper.NaverArticleMapper;
import com.example.monew.domain.article.repository.ArticleRepository;
import com.example.monew.domain.article.service.ArticleCollectionScheduler;
import com.example.monew.domain.article.service.ArticleService;
import com.example.monew.domain.interest.entity.Interest;
import com.example.monew.domain.interest.entity.Keyword;
import com.example.monew.domain.interest.repository.InterestRepository;
import com.example.monew.domain.interest.repository.KeywordRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("기사 서비스 단위 테스트")
class ArticleServiceTest {

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private InterestRepository interestRepository;

    @Mock
    private KeywordRepository keywordRepository;

    @Mock
    private NaverNewsClient naverNewsClient;

    @Mock
    private NaverArticleMapper naverArticleMapper;

    @InjectMocks
    private ArticleService articleService;

    @InjectMocks
    private ArticleCollectionScheduler articleCollectionScheduler;


    @Nested
    @DisplayName("기사 수집 테스트")
    class CollectionArticlesTest {
        @Test
        @DisplayName("정상적으로 기사를 수집할 수 있다")
        void collectArticles_success() {
            // given
            Interest interest = new Interest("경제");
            ReflectionTestUtils.setField(interest, "id", UUID.randomUUID());

            Keyword keyword = new Keyword("비트코인", interest);
            ReflectionTestUtils.setField(keyword, "id", UUID.randomUUID());

            Article article = mock(Article.class);
            Article article2 = mock(Article.class);

            NaverNewsResponse response = mock(NaverNewsResponse.class);

            when(interestRepository.findAll())
                    .thenReturn(List.of(interest));

            when(keywordRepository.findAllByInterestId(interest.getId()))
                    .thenReturn(List.of(keyword));

            when(naverNewsClient.search("비트코인"))
                    .thenReturn(response);

            when(naverArticleMapper.toArticleList(any(), anyList()))
                    .thenReturn(List.of(article, article2));

            when(articleRepository.findAllBySourceUrlIn(anySet()))
                    .thenReturn(List.of());

            when(articleRepository.saveAll(anyList()))
                    .thenReturn(List.of(article, article2));

            //when
            articleCollectionScheduler.collectArticles();

            //then
            verify(interestRepository, times(1)).findAll();
            verify(keywordRepository, times(1)).findAllByInterestId(any());

            verify(naverNewsClient, times(1)).search(anyString());
            verify(naverArticleMapper, times(1)).toArticleList(any(), anyList());

            verify(articleRepository, times(1)).findAllBySourceUrlIn(anySet());
            verify(articleRepository, times(1)).saveAll(anyList());
        }
    }
}