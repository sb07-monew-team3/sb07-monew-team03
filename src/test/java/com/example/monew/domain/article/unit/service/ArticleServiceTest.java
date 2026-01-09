package com.example.monew.domain.article.unit.service;

import com.example.monew.domain.article.client.naver.NaverNewsClient;
import com.example.monew.domain.article.client.naver.NaverNewsResponse;
import com.example.monew.domain.article.dto.*;
import com.example.monew.domain.article.entity.Article;
import com.example.monew.domain.article.mapper.ArticleMapper;
import com.example.monew.domain.article.mapper.CursorPageMapper;
import com.example.monew.domain.article.mapper.NaverArticleMapper;
import com.example.monew.domain.article.repository.ArticleRepository;
import com.example.monew.domain.article.repository.ArticleViewRepository;
import com.example.monew.domain.article.service.ArticleCollectionScheduler;
import com.example.monew.domain.article.service.ArticleService;
import com.example.monew.domain.comment.repository.CommentRepository;
import com.example.monew.domain.interest.entity.Interest;
import com.example.monew.domain.interest.entity.Keyword;
import com.example.monew.domain.interest.repository.InterestRepository;
import com.example.monew.domain.interest.repository.KeywordRepository;
import com.example.monew.global.exception.domain.article.ArticleNotExistException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("기사 서비스 단위 테스트")
class ArticleServiceTest {

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private ArticleViewRepository articleViewRepository;

    @Mock
    private InterestRepository interestRepository;

    @Mock
    private KeywordRepository keywordRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private NaverNewsClient naverNewsClient;

    @Mock
    private NaverArticleMapper naverArticleMapper;

    @Mock
    private ArticleMapper articleMapper;

    @Mock
    private CursorPageMapper cursorPageMapper;

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

    @Nested
    @DisplayName("기사 삭제 테스트")
    class DeleteArticlesTest {

        @Test
        @DisplayName("정상적으로 기사를 논리 삭제할 수 있다")
        void deleteArticleSoft_success() {
            // given
            UUID articleId = UUID.randomUUID();
            Article article = new Article(
                    Source.NAVER.getValue(),
                    "",
                    "",
                    LocalDateTime.now(),
                    "",
                    false,
                    new ArrayList<>()
            );
            ReflectionTestUtils.setField(article, "id", articleId);

            when(articleRepository.findById(articleId))
                    .thenReturn(Optional.of(article));

            when(articleRepository.save(any(Article.class)))
                    .thenReturn(mock(Article.class));

            // when
            articleService.deleteArticleSoft(articleId);

            // then
            assertThat(article.isDeleted()).isTrue();

            verify(articleRepository, times(1)).findById(articleId);
            verify(articleRepository, times(1)).save(article);
        }

        @Test
        @DisplayName("정상적으로 기사를 물리 삭제할 수 있다")
        void deleteArticleHard_success() {
            // given
            UUID articleId = UUID.randomUUID();

            when(articleRepository.findById(articleId))
                    .thenReturn(Optional.of(mock(Article.class)));

            // when
            articleService.deleteArticleHard(articleId);

            // then
            verify(articleRepository, times(1)).findById(articleId);
            verify(articleRepository, times(1)).deleteById(articleId);
        }

        @Test
        @DisplayName("존재하지 않는 기사는 논리/물리 삭제할 수 없다")
        void deleteArticle_validId_fail() {
            // given
            UUID articleId = UUID.randomUUID();

            when(articleRepository.findById(articleId))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> articleService.deleteArticleSoft(articleId))
                    .isInstanceOf(ArticleNotExistException.class);
            assertThatThrownBy(() -> articleService.deleteArticleHard(articleId))
                    .isInstanceOf(ArticleNotExistException.class);
        }
    }

    @Nested
    @DisplayName("기사 조회 테스트")
    class FindArticleTest {
        @Test
        @DisplayName("정상적으로 기사 단건을 조회할 수 있다")
        void findArticle_success() {
            // given
            UUID userId = UUID.randomUUID();
            UUID articleId = UUID.randomUUID();
            Article article = new Article(
                    Source.NAVER.getValue(),
                    "",
                    "",
                    LocalDateTime.now(),
                    "",
                    false,
                    new ArrayList<>()
            );
            ReflectionTestUtils.setField(article, "id", articleId);

            when(articleRepository.findById(articleId))
                    .thenReturn(Optional.of(article));

            when(articleMapper.toDto(article, userId))
                    .thenReturn(mock(ArticleDto.class));

            // when
            ArticleDto response = articleService.getArticle(articleId, userId);

            // then
            assertThat(response).isNotNull();

            verify(articleRepository, times(1)).findById(articleId);
        }
        
        @Test
        @DisplayName("정상적으로 기사 목록을 조회할 수 있다")
        void findArticleList_success() {
            // given
            UUID userId = UUID.randomUUID();

            ArticleRequestDto articleRequestDto = new ArticleRequestDto(
                    null,
                    null,
                    List.of(Source.NAVER),
                    null,
                    null,
                    Order.PUBLISH_DATE,
                    Direction.DESC,
                    null,
                    null,
                    10
            );

            when(cursorPageMapper.toResponseDto(any(), anyString()))
                    .thenReturn(mock(CursorPageResponseArticleDto.class));

            // when
            CursorPageResponseArticleDto response = articleService.getArticleList(articleRequestDto, userId);

            // then
            assertThat(response).isNotNull();

            verify(keywordRepository, never()).findAllByInterestId(any(UUID.class));
            verify(articleRepository, times(1)).findArticleSlice(any(ArticleRequestDto.class), any(UUID.class), anyList(), any(Pageable.class));
        }
    }
}