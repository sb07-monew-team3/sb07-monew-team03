package com.example.monew.domain.article.service;

import com.example.monew.domain.activity.mapper.UserActivityArticleViewMapper;
import com.example.monew.domain.activity.service.MongoDbService;
import com.example.monew.domain.article.dto.*;
import com.example.monew.domain.article.entity.Article;
import com.example.monew.domain.article.entity.ArticleView;
import com.example.monew.domain.article.mapper.ArticleMapper;
import com.example.monew.domain.article.mapper.ArticleViewMapper;
import com.example.monew.domain.article.mapper.CursorPageMapper;
import com.example.monew.domain.article.repository.ArticleRepository;
import com.example.monew.domain.article.repository.ArticleViewRepository;
import com.example.monew.domain.article.storage.S3ArticleStorage;
import com.example.monew.domain.interest.repository.KeywordRepository;
import com.example.monew.domain.user.entity.User;
import com.example.monew.domain.user.repository.UserRepository;
import com.example.monew.global.exception.domain.article.ArticleNotExistException;
import com.example.monew.global.exception.domain.article.InvalidSearchConditionException;
import com.example.monew.global.exception.domain.user.UserNotExistException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final ArticleViewRepository articleViewRepository;
    private final UserRepository userRepository;
    private final KeywordRepository keywordRepository;

    private final ArticleMapper articleMapper;
    private final CursorPageMapper cursorPageMapper;
    private final ArticleViewMapper articleViewMapper;

    private final S3ArticleStorage s3ArticleStorage;
    private final MongoDbService mongoDbService;
    private final UserActivityArticleViewMapper userActivityArticleViewMapper;

    @Transactional
    public void deleteArticleSoft(UUID articleId) {
        log.debug("deleteArticleSoft called with articleId={}", articleId);

        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ArticleNotExistException(articleId));

        article.deleteLogic();
        articleRepository.save(article);

        log.info("Article soft deleted: articleId={}", articleId);
    }

    @Transactional
    public void deleteArticleHard(UUID articleId) {
        log.debug("deleteArticleHard called with articleId={}", articleId);

        articleRepository.findById(articleId)
                .orElseThrow(() -> new ArticleNotExistException(articleId));

        articleRepository.deleteById(articleId);

        log.info("Article hard deleted: articleId={}", articleId);
    }

    @Transactional(readOnly = true)
    public ArticleDto getArticle(UUID articleId, UUID userId) {
        log.debug("getArticle called with articleId={}, userId={}", articleId, userId);

        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ArticleNotExistException(articleId));

        log.debug("getArticle result prepared for articleId={}", articleId);
        return articleMapper.toResponseDto(article, userId);
    }

    @Transactional(readOnly = true)
    public CursorPageResponseArticleDto getArticleList(ArticleRequestDto request, UUID userId) {
        log.debug("getArticleList called with request={}, userId={}", request, userId);

        List<String> keywords;

        // 검색어와 관심사는 동시에 들어올 수 없다
        if(!request.keyword().isBlank() && request.interestId() != null) {
            throw new InvalidSearchConditionException("키워드와 관심사가 모두 들어올 수 없습니다.");
        }

        if(!request.keyword().isBlank()) {
            keywords = List.of(request.keyword());
        } else {
            keywords = keywordRepository.findAllByInterestId(request.interestId()).stream()
                    .map(k -> k.getKeyword())
                    .collect(Collectors.toList());
        }

        // TODO: pageable은 서비스에서 하면 안되는가? 고민
        Pageable pageable = PageRequest.of(0, request.limit());

        Slice<ArticleDto> response = articleRepository.findArticleSlice(request, userId, keywords, pageable)
                .map(articleQueryDto -> articleMapper.toHighlightedDto(articleQueryDto, keywords));

        long totalElements = articleRepository.countArticleSlice(request, keywords);

        log.debug("getArticleList result prepared: totalElements={}, returned={}", totalElements, response.getContent().size());
        return cursorPageMapper.toResponseDto(response, request.orderBy(), totalElements);
    }

    @Transactional
    public ArticleViewDto recordArticleView(UUID articleId, UUID userId) {
        log.debug("recordArticleView called with articleId={}, userId={}", articleId, userId);

        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ArticleNotExistException(articleId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotExistException(userId));

        ArticleView articleView = new ArticleView(article, user);

        ArticleView saved = articleViewRepository.save(articleView);

        log.info("Article view recorded: articleId={}, userId={}", articleId, userId);
        mongoDbService.insertUserActivityArticleView(userId, userActivityArticleViewMapper.toUserActivityArticleViewDto(saved));
        return articleViewMapper.toResponseDto(saved);
    }

    @Transactional
    public ArticleRestoreResultDto restoreArticles(LocalDateTime from, LocalDateTime to) {
        log.debug("restoreArticles called with from={}, to={}", from, to);

        List<Article> articleList = s3ArticleStorage.loadArticlesFromBackup(from, to);
        List<UUID> restoredArticleIds = new ArrayList<>();

        for (Article article : articleList) {
            articleRepository.findBySourceUrl(article.getSourceUrl())
                    .ifPresentOrElse(
                            existing -> {
                                if(existing.isDeleted()) { // 논리 삭제 된 뉴스 복구
                                    existing.restoreLogic();
                                    restoredArticleIds.add(existing.getId());
                                }
                            },
                            () -> { // 물리 삭제된 뉴스 복구
                                Article saved = articleRepository.save(article);
                                restoredArticleIds.add(saved.getId());
                            }
                    );
        }

        log.debug("restoreArticles completed: restoredCount={}", restoredArticleIds.size());
        return articleMapper.toRestoreResultDto(restoredArticleIds);
    }
}
