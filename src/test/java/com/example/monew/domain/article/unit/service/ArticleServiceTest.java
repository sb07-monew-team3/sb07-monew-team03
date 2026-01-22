package com.example.monew.domain.article.unit.service;

import com.example.monew.domain.activity.dto.UserActivityArticleViewDto;
import com.example.monew.domain.activity.mapper.UserActivityArticleViewMapper;
import com.example.monew.domain.activity.service.MongoDbService;
import com.example.monew.domain.article.client.naver.NaverNewsClient;
import com.example.monew.domain.article.client.naver.NaverNewsResponse;
import com.example.monew.domain.article.client.rss.RssClient;
import com.example.monew.domain.article.dto.*;
import com.example.monew.domain.article.entity.Article;
import com.example.monew.domain.article.entity.ArticleView;
import com.example.monew.domain.article.mapper.ArticleMapper;
import com.example.monew.domain.article.mapper.ArticleViewMapper;
import com.example.monew.domain.article.mapper.CursorPageMapper;
import com.example.monew.domain.article.mapper.ApiArticleMapper;
import com.example.monew.domain.article.repository.ArticleRepository;
import com.example.monew.domain.article.repository.ArticleViewRepository;
import com.example.monew.domain.article.service.ArticleCollectionScheduler;
import com.example.monew.domain.article.service.ArticleService;
import com.example.monew.domain.article.storage.S3ArticleStorage;
import com.example.monew.domain.interest.entity.Interest;
import com.example.monew.domain.interest.entity.Keyword;
import com.example.monew.domain.interest.repository.InterestRepository;
import com.example.monew.domain.interest.repository.KeywordRepository;
import com.example.monew.domain.notification.service.NotificationService;
import com.example.monew.domain.user.entity.User;
import com.example.monew.domain.user.repository.UserRepository;
import com.example.monew.global.exception.domain.article.ArticleNotExistException;
import com.rometools.rome.feed.synd.SyndEntry;
import jakarta.persistence.criteria.CriteriaBuilder;
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

import java.time.Instant;
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
    private UserRepository userRepository;

    @Mock
    private InterestRepository interestRepository;

    @Mock
    private KeywordRepository keywordRepository;

    @Mock
    private NaverNewsClient naverNewsClient;

    @Mock
    private RssClient rssClient;

    @Mock
    private ApiArticleMapper apiArticleMapper;

    @Mock
    private ArticleMapper articleMapper;

    @Mock
    private CursorPageMapper cursorPageMapper;

    @Mock
    private ArticleViewMapper articleViewMapper;

    @Mock
    private NotificationService notificationService;

    @Mock
    private S3ArticleStorage s3ArticleStorage;

    @InjectMocks
    private ArticleService articleService;

    @InjectMocks
    private ArticleCollectionScheduler articleCollectionScheduler;

    @Mock
    private MongoDbService mongoDbService;

    @Mock
    private UserActivityArticleViewMapper userActivityArticleViewMapper;


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

            Article article = new Article(
                    "",
                    "http://www.example1@naver.com",
                    "",
                    LocalDateTime.now(),
                    "",
                    false,
                    Instant.now(),
                    List.of(interest)
            );

            Article article2 = new Article(
                    "",
                    "http://www.example2@naver.com",
                    "",
                    LocalDateTime.now().minusDays(1),
                    "",
                    false,
                    Instant.now(),
                    List.of(interest)
            );

            Article article3 = new Article(
                    "",
                    "http://www.example3@naver.com",
                    "",
                    LocalDateTime.now().minusDays(2),
                    "",
                    false,
                    Instant.now(),
                    List.of(interest)
            );

            String validRss = """
                            <?xml version="1.0" encoding="UTF-8"?>
                            <rss version="2.0">
                              <channel>
                                <title>Test RSS</title>
                                <link>https://example.com</link>
                                <description>Test RSS Feed</description>
                                <item>
                                  <title>Test Article</title>
                                  <link>https://example.com/article1</link>
                                  <pubDate>Mon, 20 Jan 2026 10:00:00 GMT</pubDate>
                                  <description>test</description>
                                </item>
                              </channel>
                            </rss>
                            """;

            NaverNewsResponse response = mock(NaverNewsResponse.class);

            when(interestRepository.findAll())
                    .thenReturn(List.of(interest));

            when(keywordRepository.findAllByInterestId(interest.getId()))
                    .thenReturn(List.of(keyword));

            when(naverNewsClient.search("비트코인"))
                    .thenReturn(response);

            when(rssClient.fetch(anyString()))
                    .thenReturn(validRss);

            when(apiArticleMapper.toArticleList(anyList(), anyList()))
                    .thenReturn(List.of(article, article2));

            when(apiArticleMapper.toArticle(any(SyndEntry.class), any(Source.class)))
                    .thenReturn(article3);

            when(articleRepository.findAllBySourceUrlIn(anySet()))
                    .thenReturn(List.of(article2));

            when(articleRepository.saveAll(anyList()))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            //when
            articleCollectionScheduler.collectArticles();

            //then
            verify(interestRepository, times(1)).findAll();
            verify(keywordRepository, times(1)).findAllByInterestId(any());

            verify(naverNewsClient, times(1)).search(anyString());
            verify(apiArticleMapper, times(1)).toArticleList(any(), anyList());

            verify(articleRepository, times(1)).findAllBySourceUrlIn(anySet());

            verify(notificationService, times(1)).createInterestAlarm(any());
            verify(articleRepository, times(2)).saveAll(anyList());
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
                    Instant.now(),
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
                    Instant.now(),
                    new ArrayList<>()
            );
            ReflectionTestUtils.setField(article, "id", articleId);

            when(articleRepository.findById(articleId))
                    .thenReturn(Optional.of(article));

            when(articleMapper.toResponseDto(article, userId))
                    .thenReturn(mock(ArticleDto.class));

            // when
            ArticleDto response = articleService.getArticle(articleId, userId);

            // then
            assertThat(response).isNotNull();

            verify(articleRepository, times(1)).findById(articleId);
        }

        @Test
        @DisplayName("정상적으로 키워드로 기사 목록을 조회할 수 있다")
        void getArticleList_withKeyword_success() {
            // given
            UUID userId = UUID.randomUUID();

            ArticleRequestDto articleRequestDto = new ArticleRequestDto(
                    "비트코인",
                    null,
                    List.of(Source.NAVER),
                    null,
                    null,
                    "publishDate",
                    "DESC",
                    null,
                    null,
                    10
            );

            List<ArticleQueryDto> contents = List.of(
                    mock(ArticleQueryDto.class)
            );

            Slice<ArticleQueryDto> slice =
                    new SliceImpl<>(contents, PageRequest.of(0, 10), false);

            when(articleRepository.findArticleSlice(any(ArticleRequestDto.class), any(UUID.class), anyList(), any(Pageable.class)))
                    .thenReturn(slice);

            when(articleRepository.countArticleSlice(any(ArticleRequestDto.class), anyList()))
                    .thenReturn(1L);

            when(cursorPageMapper.toResponseDto(any(), anyString(), anyLong()))
                    .thenReturn(mock(CursorPageResponseArticleDto.class));

            // when
            CursorPageResponseArticleDto response = articleService.getArticleList(articleRequestDto, userId);

            // then
            assertThat(response).isNotNull();

            verify(keywordRepository, never()).findAllByInterestId(any(UUID.class));
            verify(articleRepository, times(1)).findArticleSlice(any(ArticleRequestDto.class), any(UUID.class), anyList(), any(Pageable.class));
        }

        @Test
        @DisplayName("정상적으로 관심사로 기사 목록을 조회할 수 있다")
        void getArticleList_withInterest_success() {
            // given
            UUID userId = UUID.randomUUID();
            UUID interestId = UUID.randomUUID();

            Interest interest = new Interest("코인");
            ReflectionTestUtils.setField(interest, "id", interestId);

            ArticleRequestDto articleRequestDto = new ArticleRequestDto(
                    "",
                    interestId,
                    List.of(Source.NAVER),
                    null,
                    null,
                    "publishDate",
                    "DESC",
                    null,
                    null,
                    10
            );

            List<ArticleQueryDto> contents = List.of(
                    mock(ArticleQueryDto.class)
            );

            Slice<ArticleQueryDto> slice =
                    new SliceImpl<>(contents, PageRequest.of(0, 10), false);

            when(keywordRepository.findAllByInterestId(any(UUID.class)))
                    .thenReturn(List.of(new Keyword("비트코인", interest)));

            when(articleRepository.findArticleSlice(any(ArticleRequestDto.class), any(UUID.class), anyList(), any(Pageable.class)))
                    .thenReturn(slice);

            when(articleRepository.countArticleSlice(any(ArticleRequestDto.class), anyList()))
                    .thenReturn(1L);

            when(cursorPageMapper.toResponseDto(any(), anyString(), anyLong()))
                    .thenReturn(mock(CursorPageResponseArticleDto.class));

            // when
            CursorPageResponseArticleDto response = articleService.getArticleList(articleRequestDto, userId);

            // then
            assertThat(response).isNotNull();

            verify(keywordRepository, times(1)).findAllByInterestId(any(UUID.class));
            verify(articleRepository, times(1)).findArticleSlice(any(ArticleRequestDto.class), any(UUID.class), anyList(), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("기사 뷰 등록 테스트")
    class ArticleViewTest {
        @Test
        @DisplayName("정상적으로 기사 뷰를 등록할 수 있다")
        void recordArticleView_success() {
            // given
            UUID userId = UUID.randomUUID();
            UUID articleId = UUID.randomUUID();

            when(articleRepository.findById(articleId))
                    .thenReturn(Optional.of(mock(Article.class)));

            when(userRepository.findById(userId))
                    .thenReturn(Optional.of(mock(User.class)));

            when(articleViewRepository.save(any(ArticleView.class)))
                    .thenReturn(mock(ArticleView.class));

            when(articleViewMapper.toResponseDto(any(ArticleView.class)))
                    .thenReturn(mock(ArticleViewDto.class));
            when(userActivityArticleViewMapper.toUserActivityArticleViewDto(any())).thenReturn(

                    mock(UserActivityArticleViewDto.class)
            );
            doNothing().when(mongoDbService).insertUserActivityArticleView(any(),any());

            // when
            ArticleViewDto response = articleService.recordArticleView(articleId, userId);

            // then
            assertThat(response).isNotNull();

            verify(articleRepository, times(1)).findById(articleId);
            verify(userRepository, times(1)).findById(userId);
            verify(articleViewRepository, times(1)).save(any(ArticleView.class));
            verify(articleViewMapper, times(1)).toResponseDto(any(ArticleView.class));
        }
    }

    @Nested
    @DisplayName("기사 복구 테스트")
    class ArticleRestoreTest {
        @Test
        @DisplayName("정상적으로 물리 삭제 된 기사를 복구할 수 있다")
        void restoreArticle_whenHardDeleted_success() {
            // given
            LocalDateTime from = LocalDateTime.now().minusDays(1);
            LocalDateTime to = LocalDateTime.now();

            UUID articleId = UUID.randomUUID();
            UUID articleId2 = UUID.randomUUID();
            UUID articleId3 = UUID.randomUUID();

            Article article = new Article(
                    "",
                    "http://www.example1@naver.com",
                    "",
                    LocalDateTime.now(),
                    "",
                    false,
                    Instant.now(),
                    List.of(mock(Interest.class))
            );
            ReflectionTestUtils.setField(article, "id", articleId);

            Article article2 = new Article(
                    "",
                    "http://www.example2@naver.com",
                    "",
                    LocalDateTime.now().minusDays(1),
                    "",
                    false,
                    Instant.now(),
                    List.of(mock(Interest.class))
            );
            ReflectionTestUtils.setField(article2, "id", articleId2);

            Article article3 = new Article(
                    "",
                    "http://www.example3@naver.com",
                    "",
                    LocalDateTime.now().minusDays(2),
                    "",
                    true,
                    Instant.now(),
                    List.of(mock(Interest.class))
            );
            ReflectionTestUtils.setField(article3, "id", articleId3);

            when(s3ArticleStorage.loadArticlesFromBackup(from, to))
                    .thenReturn(List.of(article, article2, article3));

            when(articleRepository.findBySourceUrl(anyString()))
                    .thenReturn(Optional.empty());

            when(articleRepository.save(any(Article.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            when(articleMapper.toRestoreResultDto(anyList()))
                    .thenReturn(mock(ArticleRestoreResultDto.class));

            // when
            ArticleRestoreResultDto response = articleService.restoreArticles(from, to);

            // then
            assertThat(response).isNotNull();

            verify(s3ArticleStorage, times(1)).loadArticlesFromBackup(from, to);
            verify(articleRepository, times(3)).findBySourceUrl(anyString());
            verify(articleRepository, times(3)).save(any(Article.class));
            verify(articleMapper, times(1)).toRestoreResultDto(anyList());
        }
        
        @Test
        @DisplayName("정상적으로 논리 삭제된 기사를 복구할 수 있다")
        void restoreArticle_whenSoftDeleted_success() {
            // given
            LocalDateTime from = LocalDateTime.now().minusDays(1);
            LocalDateTime to = LocalDateTime.now();

            UUID articleId = UUID.randomUUID();
            UUID articleId2 = UUID.randomUUID();
            UUID articleId3 = UUID.randomUUID();

            Article article = new Article(
                    "",
                    "http://www.example1@naver.com",
                    "",
                    LocalDateTime.now(),
                    "",
                    true,
                    Instant.now(),
                    List.of(mock(Interest.class))
            );
            ReflectionTestUtils.setField(article, "id", articleId);

            Article article2 = new Article(
                    "",
                    "http://www.example2@naver.com",
                    "",
                    LocalDateTime.now().minusDays(1),
                    "",
                    true,
                    Instant.now(),
                    List.of(mock(Interest.class))
            );
            ReflectionTestUtils.setField(article2, "id", articleId2);

            Article article3 = new Article(
                    "",
                    "http://www.example3@naver.com",
                    "",
                    LocalDateTime.now().minusDays(2),
                    "",
                    true,
                    Instant.now(),
                    List.of(mock(Interest.class))
            );
            ReflectionTestUtils.setField(article3, "id", articleId3);

            when(s3ArticleStorage.loadArticlesFromBackup(from, to))
                    .thenReturn(List.of(article, article2, article3));

            when(articleRepository.findBySourceUrl(article.getSourceUrl()))
                    .thenReturn(Optional.of(article));

            when(articleRepository.findBySourceUrl(article2.getSourceUrl()))
                    .thenReturn(Optional.of(article2));

            when(articleRepository.findBySourceUrl(article3.getSourceUrl()))
                    .thenReturn(Optional.of(article3));

            when(articleMapper.toRestoreResultDto(anyList()))
                    .thenReturn(mock(ArticleRestoreResultDto.class));

            // when
            ArticleRestoreResultDto response = articleService.restoreArticles(from, to);

            // then
            assertThat(response).isNotNull();

            verify(s3ArticleStorage, times(1)).loadArticlesFromBackup(from, to);
            verify(articleRepository, times(3)).findBySourceUrl(anyString());
            verify(articleRepository, never()).save(any(Article.class));
            verify(articleMapper, times(1)).toRestoreResultDto(anyList());
        }
    }
}