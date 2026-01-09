package com.example.monew.domain.article.service;

import com.example.monew.domain.article.dto.ArticleDto;
import com.example.monew.domain.article.dto.ArticleRequestDto;
import com.example.monew.domain.article.dto.CursorPageResponseArticleDto;
import com.example.monew.domain.article.entity.Article;
import com.example.monew.domain.article.mapper.ArticleMapper;
import com.example.monew.domain.article.mapper.CursorPageMapper;
import com.example.monew.domain.article.repository.ArticleRepository;
import com.example.monew.domain.interest.repository.KeywordRepository;
import com.example.monew.global.exception.domain.article.ArticleNotExistException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final KeywordRepository keywordRepository;

    private final ArticleMapper articleMapper;
    private final CursorPageMapper cursorPageMapper;

    public void deleteArticleSoft(UUID articleId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ArticleNotExistException(articleId));

        article.deleteLogic();
        articleRepository.save(article);
    }

    public void deleteArticleHard(UUID articleId) {
        articleRepository.findById(articleId)
                .orElseThrow(() -> new ArticleNotExistException(articleId));

        articleRepository.deleteById(articleId);
    }

    public ArticleDto getArticle(UUID articleId, UUID userId) {

        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ArticleNotExistException(articleId));

        return articleMapper.toDto(article, userId);
    }

    public CursorPageResponseArticleDto getArticleList(ArticleRequestDto request, UUID userId) {

        List<String> keywords;

        // 검색어와 관심사는 동시에 들어올 수 없다
        if(request.keyword() != null && request.interestedId() != null) {
            throw new IllegalArgumentException("키워드와 관심사가 모두 들어올 수 없습니다."); // TODO: 커스텀 예외 추가 필요
        }

        if(request.keyword() != null) {
            keywords = List.of(request.keyword());
        } else {
            keywords = keywordRepository.findAllByInterestId(request.interestedId()).stream()
                    .map(k -> k.getKeyword())
                    .collect(Collectors.toList());
        }

        // TODO: pageable은 서비스에서 하면 안되는가? 고민
        Pageable pageable = PageRequest.of(0, request.limit(),
                Sort.by(request.direction(), request.orderBy().getValue()));

        Page<ArticleDto> response = articleRepository.findArticleSlice(request, userId, keywords, pageable);

        return cursorPageMapper.toResponseDto(response, request.orderBy().getValue());
    }
}
