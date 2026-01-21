package com.example.monew.domain.article.unit.repository;

import com.example.monew.domain.article.dto.ArticleQueryDto;
import com.example.monew.domain.article.dto.ArticleRequestDto;
import com.example.monew.domain.article.dto.Source;
import com.example.monew.domain.article.entity.Article;
import com.example.monew.domain.article.entity.ArticleView;
import com.example.monew.domain.article.repository.ArticleRepository;
import com.example.monew.domain.article.repository.ArticleViewRepository;
import com.example.monew.domain.comment.entity.Comment;
import com.example.monew.domain.comment.repository.CommentRepository;
import com.example.monew.domain.interest.entity.Interest;
import com.example.monew.domain.interest.repository.InterestRepository;
import com.example.monew.domain.notification.repository.NotificationRepository;
import com.example.monew.domain.user.entity.User;
import com.example.monew.domain.user.repository.UserRepository;
import com.example.monew.global.config.JpaAuditingConfig;
import com.example.monew.global.config.QueryDslConfig;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // 테스트에서도 설정된 DB 사용
@Import({QueryDslConfig.class, JpaAuditingConfig.class}) // Bean 수동 등록
@DisplayName("뉴스 기사 레포지토리 슬라이스 테스트")
@Transactional
class ArticleRepositoryTest {

    @Autowired
    private ArticleViewRepository articleViewRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private InterestRepository interestRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager em;

    private boolean alreadySetup = false;
    private List<User> users;
    private Interest backend, hardware, infra, database, security, cloud;
    private Article article1, article2, article3, article4, article5,
            article6, article7, article8, article9, article10,
            article11, article12, article13, article14, article15,
            article16, article17, article18, article19, article20;

    private final LocalDateTime baseDate = LocalDateTime.of(2026, 1, 1, 0, 0);
    private final Instant baseInstant = Instant.parse("2026-01-01T00:00:00Z");


    @BeforeEach
    void setUp() {
        if(alreadySetup) return;

        notificationRepository.deleteAll();
        articleViewRepository.deleteAll();
        userRepository.deleteAll();
        articleRepository.deleteAll();
        interestRepository.deleteAll();
        commentRepository.deleteAll();

        em.flush();
        em.clear();


        // 사용자 데이터 생성
        users = new ArrayList<>();

        for (int i = 0; i <= 20; i++) {
            User user = new User(
                    "test" + i + "@naver.com",
                    "테스트" + i,
                    "test12345",
                    null
            );
            users.add(user);
        }

        userRepository.saveAll(users);


        // 관심사 데이터 생성
        backend = new Interest("백엔드");
        hardware = new Interest("하드웨어");
        infra = new Interest("인프라");
        database = new Interest("데이터베이스");
        security = new Interest("보안");
        cloud = new Interest("클라우드");
        interestRepository.saveAll(List.of(backend, hardware, infra, database, security, cloud));


        // 뉴스 기사 데이터 생성
        article1 = new Article(
                "NAVER",
                "https://devnews.com/spring-boot",
                "Spring Boot 3에서 개선된 성능 분석",
                baseDate.minusDays(2),
                "Spring Boot 3 성능 개선",
                false,
                baseInstant,
                List.of(backend)
        );

        article2 = new Article(
                "NAVER",
                "https://devnews.com/cpu-guide",
                "백엔드 서버를 위한 CPU 선택 기준",
                baseDate.minusDays(1),
                "서버 CPU 선택 가이드",
                false,
                baseInstant.plusSeconds(10),
                List.of(hardware)
        );

        article3 = new Article(
                "NAVER",
                "https://devnews.com/webflux",
                "동시성 처리 관점에서 비교",
                baseDate,
                "Spring MVC vs WebFlux",
                false,
                baseInstant.plusSeconds(20),
                List.of(backend)
        );

        article4 = new Article(
                "NAVER",
                "https://devnews.com/jpa-batch",
                "JPA 배치 처리 성능 튜닝 방법",
                baseDate.minusDays(3),
                "Hibernate batch size 최적화",
                false,
                baseInstant.plusSeconds(30),
                List.of(backend)
        );

        article5 = new Article(
                "NAVER",
                "https://devnews.com/querydsl",
                "QueryDSL 동적 쿼리 설계 패턴",
                baseDate.minusDays(4),
                "BooleanExpression 활용법",
                false,
                baseInstant.plusSeconds(40),
                List.of(backend)
        );

        article6 = new Article(
                "NAVER",
                "https://devnews.com/gc",
                "Java GC 로그 분석 실전 가이드",
                baseDate.minusDays(5),
                "G1 GC 튜닝 포인트",
                false,
                baseInstant.plusSeconds(50),
                List.of(backend)
        );

        article7 = new Article(
                "NAVER",
                "https://devnews.com/memory",
                "JVM 메모리 구조와 힙 최적화",
                baseDate.minusDays(6),
                "Young / Old 영역 분석",
                false,
                baseInstant.plusSeconds(60),
                List.of(backend)
        );

        article8 = new Article(
                "NAVER",
                "https://devnews.com/docker",
                "Docker 컨테이너 리소스 제한 전략",
                baseDate.minusDays(7),
                "CPU / Memory 제한 설정",
                false,
                baseInstant.plusSeconds(70),
                List.of(infra)
        );

        article9 = new Article(
                "NAVER",
                "https://devnews.com/kubernetes",
                "Kubernetes 파드 스케줄링 원리",
                baseDate.minusDays(8),
                "노드 리소스 관리",
                false,
                baseInstant.plusSeconds(80),
                List.of(infra)
        );

        article10 = new Article(
                "NAVER",
                "https://devnews.com/mysql-index",
                "MySQL 인덱스 설계 전략",
                baseDate.minusDays(9),
                "복합 인덱스 활용법",
                false,
                baseInstant.plusSeconds(90),
                List.of(database)
        );

        article11 = new Article(
                "NAVER",
                "https://devnews.com/redis",
                "Redis 캐시 전략과 TTL 설계",
                baseDate.minusDays(10),
                "캐시 일관성 유지",
                false,
                baseInstant.plusSeconds(100),
                List.of(database)
        );

        article12 = new Article(
                "NAVER",
                "https://devnews.com/elasticsearch",
                "Elasticsearch 검색 성능 최적화",
                baseDate.minusDays(11),
                "Analyzer 선택 기준",
                false,
                baseInstant.plusSeconds(110),
                List.of(database)
        );

        article13 = new Article(
                "NAVER",
                "https://devnews.com/netty",
                "Netty 기반 비동기 서버 구조",
                baseDate.minusDays(12),
                "이벤트 루프 모델",
                false,
                baseInstant.plusSeconds(120),
                List.of(backend)
        );

        article14 = new Article(
                "NAVER",
                "https://devnews.com/security-jwt",
                "JWT 인증 구조 설계 시 주의점",
                baseDate.minusDays(13),
                "Access Token 만료 전략",
                false,
                baseInstant.plusSeconds(130),
                List.of(security)
        );

        article15 = new Article(
                "NAVER",
                "https://devnews.com/oauth2",
                "OAuth2 인증 플로우 정리",
                baseDate.minusDays(14),
                "Authorization Code Flow",
                false,
                baseInstant.plusSeconds(140),
                List.of(security)
        );

        article16 = new Article(
                "NAVER",
                "https://devnews.com/cloud-cost",
                "클라우드 비용 최적화 방법",
                baseDate.minusDays(15),
                "AWS 비용 절감 전략",
                false,
                baseInstant.plusSeconds(150),
                List.of(cloud)
        );

        article17 = new Article(
                "NAVER",
                "https://devnews.com/ec2",
                "EC2 인스턴스 타입 선택 가이드",
                baseDate.minusDays(16),
                "워크로드별 인스턴스 추천",
                false,
                baseInstant.plusSeconds(160),
                List.of(cloud)
        );

        article18 = new Article(
                "NAVER",
                "https://devnews.com/cicd",
                "CI/CD 파이프라인 설계 베스트 프랙티스",
                baseDate.minusDays(17),
                "GitHub Actions 활용",
                false,
                baseInstant.plusSeconds(170),
                List.of(infra)
        );

        article19 = new Article(
                "NAVER",
                "https://devnews.com/monitoring",
                "서버 모니터링 지표 설계",
                baseDate.minusDays(18),
                "Prometheus & Grafana",
                false,
                baseInstant.plusSeconds(180),
                List.of(infra)
        );

        article20 = new Article(
                "NAVER",
                "https://devnews.com/troubleshooting",
                "대규모 장애 대응 사례 분석",
                baseDate.minusDays(19),
                "트래픽 폭주 대응 전략",
                false,
                baseInstant.plusSeconds(190),
                List.of(infra)
        );

        articleRepository.saveAll(List.of(
                article1, article2, article3, article4, article5,
                article6, article7, article8, article9, article10,
                article11, article12, article13, article14, article15,
                article16, article17, article18, article19, article20
        ));

        em.flush();

        // 조회수 데이터 생성
        List<ArticleView> articleViews = new ArrayList<>();
        List<Article> articleList = List.of(
                article1, article2, article3, article4, article5,
                article6, article7, article8, article9, article10,
                article11, article12, article13, article14, article15,
                article16, article17, article18, article19, article20
        );
        int[] fixedViewCounts = {15, 8, 20, 5, 12, 10, 7, 14, 9, 6, 11, 13, 4, 18, 3, 16, 19, 2, 1, 17};

        for (int i = 0; i < articleList.size(); i++) {
            Article article = articleList.get(i);
            int viewCount = fixedViewCounts[i];

            for (int j = 0; j < viewCount; j++) {
                ArticleView view = new ArticleView(article, users.get(j));
                articleViews.add(view);
            }
        }
        articleViewRepository.saveAll(articleViews);

        // 댓글 데이터 생성
        List<Comment> comments = new ArrayList<>();

        comments.addAll(createComments(users.get(0), article1, 5, "Spring Boot"));
        comments.addAll(createComments(users.get(0), article2, 1, "CPU"));
        comments.addAll(createComments(users.get(0), article3, 10, "WebFlux"));
        comments.addAll(createComments(users.get(0), article4, 3, "JPA Batch"));
        comments.addAll(createComments(users.get(0), article5, 7, "QueryDSL"));
        comments.addAll(createComments(users.get(0), article6, 0, "GC"));
        comments.addAll(createComments(users.get(0), article7, 2, "JVM Memory"));
        comments.addAll(createComments(users.get(0), article8, 8, "Docker"));
        comments.addAll(createComments(users.get(0), article9, 4, "Kubernetes"));
        comments.addAll(createComments(users.get(0), article10, 6, "MySQL Index"));
        comments.addAll(createComments(users.get(0), article11, 9, "Redis"));
        comments.addAll(createComments(users.get(0), article12, 1, "Elasticsearch"));
        comments.addAll(createComments(users.get(0), article13, 0, "Netty"));
        comments.addAll(createComments(users.get(0), article14, 12, "JWT"));
        comments.addAll(createComments(users.get(0), article15, 2, "OAuth2"));
        comments.addAll(createComments(users.get(0), article16, 5, "Cloud Cost"));
        comments.addAll(createComments(users.get(0), article17, 11, "EC2"));
        comments.addAll(createComments(users.get(0), article18, 3, "CI/CD"));
        comments.addAll(createComments(users.get(0), article19, 6, "Monitoring"));
        comments.addAll(createComments(users.get(0), article20, 4, "Troubleshooting"));

        commentRepository.saveAll(comments);

        em.flush();
        em.clear();

        alreadySetup = true;
    }

    private List<Comment> createComments(
            User user,
            Article article,
            int count,
            String prefix
    ) {
        List<Comment> comments = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            comments.add(
                    new Comment(
                            user,
                            article,
                            prefix + " 댓글 " + i,
                            false
                    )
            );
        }

        return comments;
    }

    @Test
    @DisplayName("sourceUrl 목록으로 기사들을 조회한다")
    void findAllBySourceUrlIn_success() {
        // given
        Set<String> urls = Set.of(
                "https://devnews.com/spring-boot",
                "https://devnews.com/cpu-guide"
        );

        // when
        List<Article> articles =
                articleRepository.findAllBySourceUrlIn(urls);

        // then
        assertThat(articles).hasSize(2);
        assertThat(articles)
                .extracting(Article::getTitle)
                .containsExactlyInAnyOrder(
                        "Spring Boot 3에서 개선된 성능 분석",
                        "백엔드 서버를 위한 CPU 선택 기준"
                );
    }

    @Nested
    @DisplayName("기사 목록 조회 슬라이스 테스트")
    class getArticleListTest {
        @Test
        @DisplayName("키워드로 기사 목록을 Slice 형태로 조회한다")
        void findArticleSlice_byKeyword_success() {
            // given
            UUID userId = UUID.randomUUID();

            ArticleRequestDto request = new ArticleRequestDto(
                    "Spring",
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

            Pageable pageable = PageRequest.of(0, 10);
            List<String> keywords = List.of("Spring");

            // when
            Slice<ArticleQueryDto> slice =
                    articleRepository.findArticleSlice(request, userId, keywords, pageable);

            // then
            assertThat(slice).isNotNull();
            assertThat(slice.getContent()).hasSize(2);
            assertThat(slice.hasNext()).isFalse();

            assertThat(slice.getContent())
                    .extracting(ArticleQueryDto::title)
                    .containsExactly(
                            "동시성 처리 관점에서 비교",
                            "Spring Boot 3에서 개선된 성능 분석"
                    );
        }

        @Test
        @DisplayName("publishDateFrom만 전달되면 해당 날짜 이후 기사만 조회된다")
        void findArticleSlice_publishDateFrom_only() {

            // given
            LocalDateTime start = baseDate.minusDays(2);

            ArticleRequestDto request = new ArticleRequestDto(
                    null,
                    null,
                    List.of(Source.NAVER),
                    start,
                    null,
                    "publishDate",
                    "ASC",
                    null,
                    null,
                    10
            );

            Pageable pageable = PageRequest.of(0, 10);

            // when
            Slice<ArticleQueryDto> result =
                    articleRepository.findArticleSlice(request, users.get(0).getId(), null, pageable);

            // then
            assertThat(result).hasSize(3);
            assertThat(result)
                    .extracting(ArticleQueryDto::sourceUrl)
                    .containsExactly(
                            "https://devnews.com/spring-boot",
                            "https://devnews.com/cpu-guide",
                            "https://devnews.com/webflux"
                    );
        }

        @Test
        @DisplayName("publishDateTo만 전달되면 해당 날짜 이전 기사만 조회된다")
        void findArticleSlice_publishDateTo_only() {

            // given
            LocalDateTime end = baseDate.minusDays(10);

            ArticleRequestDto request = new ArticleRequestDto(
                    null,
                    null,
                    List.of(Source.NAVER),
                    null,
                    end,
                    "publishDate",
                    "ASC",
                    null,
                    null,
                    10
            );

            Pageable pageable = PageRequest.of(0, 10);

            // when
            Slice<ArticleQueryDto> result =
                    articleRepository.findArticleSlice(request, users.get(0).getId(), null, pageable);

            // then
            assertThat(result).hasSize(10);
            assertThat(result)
                    .extracting(ArticleQueryDto::sourceUrl)
                    .containsExactly(
                            "https://devnews.com/troubleshooting",
                            "https://devnews.com/monitoring",
                            "https://devnews.com/cicd",
                            "https://devnews.com/ec2",
                            "https://devnews.com/cloud-cost",
                            "https://devnews.com/oauth2",
                            "https://devnews.com/security-jwt",
                            "https://devnews.com/netty",
                            "https://devnews.com/elasticsearch",
                            "https://devnews.com/redis"
                    );
        }

        @Test
        @DisplayName("publishDateFrom과 publishDateTo를 함께 전달하면 범위 내 기사만 조회된다")
        void findArticleSlice_publishDateBetween() {

            // given
            LocalDateTime start = baseDate.minusDays(20);
            LocalDateTime end = baseDate.minusDays(15);

            ArticleRequestDto request = new ArticleRequestDto(
                    null,
                    null,
                    List.of(Source.NAVER),
                    start,
                    end,
                    "publishDate",
                    "ASC",
                    null,
                    null,
                    10
            );

            Pageable pageable = PageRequest.of(0, 10);

            // when
            Slice<ArticleQueryDto> result =
                    articleRepository.findArticleSlice(request, users.get(0).getId(), null, pageable);

            // then
            assertThat(result).hasSize(5);
            assertThat(result)
                    .extracting(ArticleQueryDto::sourceUrl)
                    .containsExactlyInAnyOrder(
                            "https://devnews.com/troubleshooting",
                            "https://devnews.com/monitoring",
                            "https://devnews.com/cicd",
                            "https://devnews.com/ec2",
                            "https://devnews.com/cloud-cost"
                    );
        }

        @Test
        @DisplayName("publishDate DESC 정렬 시 최신 기사부터 조회된다")
        void findArticleSlice_orderByPublishDateDesc() {

            // given
            ArticleRequestDto request = new ArticleRequestDto(
                    null,
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

            Pageable pageable = PageRequest.of(0, 10);

            // when
            Slice<ArticleQueryDto> result =
                    articleRepository.findArticleSlice(
                            request,
                            users.get(0).getId(),
                            null,
                            pageable
                    );

            // then
            assertThat(result.getContent())
                    .extracting(ArticleQueryDto::publishDate)
                    .isSortedAccordingTo(Comparator.reverseOrder());

            assertThat(result.getContent().get(0).sourceUrl())
                    .isEqualTo("https://devnews.com/webflux");
        }

        @Test
        @DisplayName("publishDate ASC 정렬 시 오래된 기사부터 조회된다")
        void findArticleSlice_orderByPublishDateAsc() {

            // given
            ArticleRequestDto request = new ArticleRequestDto(
                    null,
                    null,
                    List.of(Source.NAVER),
                    null,
                    null,
                    "publishDate",
                    "ASC",
                    null,
                    null,
                    10
            );

            Pageable pageable = PageRequest.of(0, 10);

            // when
            Slice<ArticleQueryDto> result =
                    articleRepository.findArticleSlice(
                            request,
                            users.get(0).getId(),
                            null,
                            pageable
                    );

            // then
            assertThat(result.getContent())
                    .extracting(ArticleQueryDto::publishDate)
                    .isSorted();

            assertThat(result.getContent().get(0).sourceUrl())
                    .isEqualTo("https://devnews.com/troubleshooting");
        }

        @Test
        @DisplayName("댓글 수 기준 내림차순으로 기사 목록을 조회한다")
        void findArticleSlice_sortByCommentCount_desc() {
            // given
            ArticleRequestDto request = new ArticleRequestDto(
                    null,
                    null,
                    List.of(Source.NAVER),
                    null,
                    null,
                    "commentCount",
                    "DESC",
                    null,
                    null,
                    10
            );

            Pageable pageable = PageRequest.of(0, 10);

            // when
            Slice<ArticleQueryDto> slice =
                    articleRepository.findArticleSlice(request, users.get(0).getId(), null, pageable);

            // then
            assertThat(slice).isNotNull();
            assertThat(slice.getContent()).hasSize(10);
            assertThat(slice.hasNext()).isTrue();

            assertThat(slice.getContent())
                    .extracting(ArticleQueryDto::sourceUrl)
                    .containsExactly(
                            article14.getSourceUrl(), // 12
                            article17.getSourceUrl(), // 11
                            article3.getSourceUrl(),  // 10
                            article11.getSourceUrl(), // 9
                            article8.getSourceUrl(),  // 8
                            article5.getSourceUrl(),  // 7
                            article19.getSourceUrl(), // 6
                            article10.getSourceUrl(), // 6
                            article16.getSourceUrl(),  // 5
                            article1.getSourceUrl()  // 5
                    );
        }

        @Test
        @DisplayName("댓글 수 기준 오름차순으로 기사 목록을 조회한다")
        void findArticleSlice_sortByCommentCount_asc() {
            // given
            ArticleRequestDto request = new ArticleRequestDto(
                    null,
                    null,
                    List.of(Source.NAVER),
                    null,
                    null,
                    "commentCount",
                    "ASC",
                    null,
                    null,
                    10
            );

            Pageable pageable = PageRequest.of(0, 10);

            // when
            Slice<ArticleQueryDto> slice =
                    articleRepository.findArticleSlice(
                            request,
                            users.get(0).getId(),
                            null,
                            pageable
                    );

            // then
            assertThat(slice).isNotNull();
            assertThat(slice.getContent()).hasSize(10);
            assertThat(slice.hasNext()).isTrue();

            assertThat(slice.getContent())
                    .extracting(ArticleQueryDto::sourceUrl)
                    .containsExactly(
                            article6.getSourceUrl(),   // 0
                            article13.getSourceUrl(),  // 0
                            article2.getSourceUrl(),   // 1
                            article12.getSourceUrl(),  // 1
                            article7.getSourceUrl(),   // 2
                            article15.getSourceUrl(),  // 2
                            article4.getSourceUrl(),   // 3
                            article18.getSourceUrl(),  // 3
                            article9.getSourceUrl(),   // 4
                            article20.getSourceUrl()   // 4
                    );
        }

        @Test
        @DisplayName("조회수 기준 내림차순으로 기사 목록을 조회한다")
        void findArticleSlice_sortByViewCount_desc() {
            // given
            ArticleRequestDto request = new ArticleRequestDto(
                    null,
                    null,
                    List.of(Source.NAVER),
                    null,
                    null,
                    "viewCount",
                    "DESC",
                    null,
                    null,
                    10
            );

            Pageable pageable = PageRequest.of(0, 10);

            // when
            Slice<ArticleQueryDto> slice =
                    articleRepository.findArticleSlice(request, users.get(0).getId(), null, pageable);

            // then
            assertThat(slice).isNotNull();
            assertThat(slice.getContent()).hasSize(10);
            assertThat(slice.hasNext()).isTrue();
            assertThat(slice.getContent())
                    .extracting(ArticleQueryDto::sourceUrl)
                    .containsExactly(
                            article3.getSourceUrl(),  // 20
                            article17.getSourceUrl(), // 19
                            article14.getSourceUrl(), // 18
                            article20.getSourceUrl(), // 17
                            article16.getSourceUrl(), // 16
                            article1.getSourceUrl(),  // 15
                            article8.getSourceUrl(),  // 14
                            article12.getSourceUrl(), // 13
                            article5.getSourceUrl(),  // 12
                            article11.getSourceUrl()  // 11
                    );
        }

        @Test
        @DisplayName("조회수 기준 오름차순으로 기사 목록을 조회한다")
        void findArticleSlice_sortByViewCount_asc() {
            // given
            ArticleRequestDto request = new ArticleRequestDto(
                    null,
                    null,
                    List.of(Source.NAVER),
                    null,
                    null,
                    "viewCount",
                    "ASC",
                    null,
                    null,
                    10
            );

            Pageable pageable = PageRequest.of(0, 10);

            // when
            Slice<ArticleQueryDto> slice =
                    articleRepository.findArticleSlice(request, users.get(0).getId(), null, pageable);

            // then
            assertThat(slice).isNotNull();
            assertThat(slice.getContent()).hasSize(10);
            assertThat(slice.hasNext()).isTrue();
            assertThat(slice.getContent())
                    .extracting(ArticleQueryDto::sourceUrl)
                    .containsExactly(
                            article19.getSourceUrl(), // 1
                            article18.getSourceUrl(), // 2
                            article15.getSourceUrl(), // 3
                            article13.getSourceUrl(), // 4
                            article4.getSourceUrl(),  // 5
                            article10.getSourceUrl(), // 6
                            article7.getSourceUrl(),  // 7
                            article2.getSourceUrl(),  // 8
                            article9.getSourceUrl(),  // 9
                            article6.getSourceUrl()   // 10
                    );
        }

        @Test
        @DisplayName("커서를 사용해 publishDate 기준 DESC 조회")
        void findArticleSlice_cursor_publishDate_desc() {
            // given
            Article latestArticle = article3;
            String cursor = latestArticle.getPublishDate().toString();
            Instant after = latestArticle.getSortTimestamp();

            ArticleRequestDto request = new ArticleRequestDto(
                    null,
                    null,
                    List.of(Source.NAVER),
                    null,
                    null,
                    "publishDate",
                    "DESC",
                    cursor,
                    after,
                    10
            );

            Pageable pageable = PageRequest.of(0, 10);

            // when
            Slice<ArticleQueryDto> slice = articleRepository.findArticleSlice(request, users.get(0).getId(), null, pageable);

            // then
            assertThat(slice).isNotNull();
            // cursor 이후(이전) 기사 10개 조회
            assertThat(slice.getContent())
                    .extracting(ArticleQueryDto::publishDate)
                    .allMatch(d -> d.isBefore(latestArticle.getPublishDate())
                            || d.isEqual(latestArticle.getPublishDate()));
        }

        @Test
        @DisplayName("커서를 사용해 publishDate 기준 ASC 조회")
        void findArticleSlice_cursor_publishDate_asc() {
            // given
            Article oldestArticle = article20;
            String cursor = oldestArticle.getPublishDate().toString();
            Instant after = oldestArticle.getSortTimestamp();

            ArticleRequestDto request = new ArticleRequestDto(
                    null,
                    null,
                    List.of(Source.NAVER),
                    null,
                    null,
                    "publishDate",
                    "ASC",
                    cursor,
                    after,
                    10
            );

            Pageable pageable = PageRequest.of(0, 10);

            // when
            Slice<ArticleQueryDto> slice = articleRepository.findArticleSlice(request, users.get(0).getId(), null, pageable);

            // then
            assertThat(slice).isNotNull();
            assertThat(slice.getContent())
                    .extracting(ArticleQueryDto::publishDate)
                    .allMatch(d -> d.isAfter(oldestArticle.getPublishDate())
                            || d.isEqual(oldestArticle.getPublishDate())); // ASC니까 오래된 이후
        }

        @Test
        @DisplayName("조회수 기준 DESC 커서 조회")
        void findArticleSlice_cursorViewCount_desc() {
            // given
            long cursor = 15;
            Instant after = baseInstant;
            ArticleRequestDto request = new ArticleRequestDto(
                    null,
                    null,
                    List.of(Source.NAVER),
                    null,
                    null,
                    "viewCount",
                    "DESC",
                    String.valueOf(cursor),
                    after,
                    10
            );

            Pageable pageable = PageRequest.of(0, 10);

            // when
            Slice<ArticleQueryDto> slice = articleRepository.findArticleSlice(request, users.get(0).getId(), null, pageable);

            // then
            assertThat(slice).isNotNull();
            assertThat(slice.getContent()).isNotEmpty();
            assertThat(slice.getContent())
                    .extracting(ArticleQueryDto::viewCount)
                    .allMatch(count -> count <= cursor);
        }

        @Test
        @DisplayName("조회수 기준 ASC 커서 조회")
        void findArticleSlice_cursorViewCount_asc() {
            // given
            long cursor = 5;
            Instant after = baseInstant;
            ArticleRequestDto request = new ArticleRequestDto(
                    null,
                    null,
                    List.of(Source.NAVER),
                    null,
                    null,
                    "viewCount",
                    "ASC",
                    String.valueOf(cursor),
                    after,
                    10
            );

            Pageable pageable = PageRequest.of(0, 10);

            // when
            Slice<ArticleQueryDto> slice = articleRepository.findArticleSlice(request, users.get(0).getId(), null, pageable);

            // then
            assertThat(slice.getContent())
                    .extracting(ArticleQueryDto::viewCount)
                    .allMatch(count -> count > cursor || count == cursor);
        }

        @Test
        @DisplayName("댓글 수 기준 DESC 커서 조회")
        void findArticleSlice_cursorCommentCount_desc() {
            // given
            long cursor = 10;
            Instant after = baseInstant;
            ArticleRequestDto request = new ArticleRequestDto(
                    null,
                    null,
                    List.of(Source.NAVER),
                    null,
                    null,
                    "commentCount",
                    "DESC",
                    String.valueOf(cursor),
                    after,
                    10
            );

            Pageable pageable = PageRequest.of(0, 10);

            // when
            Slice<ArticleQueryDto> slice = articleRepository.findArticleSlice(request, users.get(0).getId(), null, pageable);

            // then
            assertThat(slice).isNotNull();
            assertThat(slice.getContent()).isNotEmpty();
            assertThat(slice.getContent())
                    .extracting(ArticleQueryDto::commentCount)
                    .allMatch(count -> count < cursor || count == cursor);
        }

        @Test
        @DisplayName("댓글 수 기준 ASC 커서 조회")
        void findArticleSlice_cursorCommentCount_asc() {
            // given
            long cursor = 5;
            Instant after = baseInstant;
            ArticleRequestDto request = new ArticleRequestDto(
                    null,
                    null,
                    List.of(Source.NAVER),
                    null,
                    null,
                    "commentCount",
                    "ASC",
                    String.valueOf(cursor),
                    after,
                    10
            );

            Pageable pageable = PageRequest.of(0, 10);

            // when
            Slice<ArticleQueryDto> slice = articleRepository.findArticleSlice(request, users.get(0).getId(), null, pageable);

            // then
            assertThat(slice).isNotNull();
            assertThat(slice.getContent()).isNotEmpty();
            assertThat(slice.getContent())
                    .extracting(ArticleQueryDto::commentCount)
                    .allMatch(count -> count > cursor || count == cursor);
        }
    }

    @Test
    @DisplayName("키워드 조건에 맞는 기사 개수를 카운트한다")
    void countArticleSlice_success() {
        // given
        ArticleRequestDto request = new ArticleRequestDto(
                "서버",
                null,
                null,
                null,
                null,
                "publishDate",
                "DESC",
                null,
                null,
                10
        );

        List<String> keywords = List.of("서버");

        // when
        long count = articleRepository.countArticleSlice(request, keywords);

        // then
        assertThat(count).isEqualTo(3);
    }

    @Test
    @DisplayName("발행일 기준 기간 내 기사만 조회한다")
    void findAllByPublishDateBetween_success() {
        // given
        LocalDateTime start = baseDate.minusDays(1);
        LocalDateTime end = baseDate.minusDays(1);

        // when
        List<Article> articles =
                articleRepository.findAllByPublishDateBetween(start, end);

        // then
        assertThat(articles).hasSize(1);
        assertThat(articles.get(0).getTitle())
                .isEqualTo("백엔드 서버를 위한 CPU 선택 기준");
    }

    @Test
    @DisplayName("sourceUrl로 단일 기사를 조회한다")
    void findBySourceUrl_success() {
        // when
        Optional<Article> article =
                articleRepository.findBySourceUrl("https://devnews.com/webflux");

        // then
        assertThat(article).isPresent();
        assertThat(article.get().getTitle())
                .isEqualTo("동시성 처리 관점에서 비교");
    }

}