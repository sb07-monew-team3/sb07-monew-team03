package com.example.monew.domain.article.mapper;

import com.example.monew.domain.article.dto.ArticleDto;
import com.example.monew.domain.article.dto.ArticleQueryDto;
import com.example.monew.domain.article.dto.ArticleRestoreResultDto;
import com.example.monew.domain.article.entity.Article;
import com.example.monew.domain.article.repository.ArticleViewRepository;
import com.example.monew.domain.comment.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class ArticleMapper {
    private final ArticleViewRepository articleViewRepository;
    private final CommentRepository commentRepository;

    public ArticleDto toResponseDto(Article article, UUID userId) {
        boolean viewedByMe = articleViewRepository.existsByArticleIdAndUserId(article.getId(), userId);

        long commentCount = commentRepository.countByArticleId(article.getId());
        long viewCount = articleViewRepository.countByArticleId(article.getId());

        return new ArticleDto(
                article.getId(),
                article.getSource(),
                article.getSourceUrl(),
                article.getTitle(),
                article.getPublishDate(),
                article.getSummary(),
                commentCount,
                viewCount,
                viewedByMe
        );
    }

    public ArticleRestoreResultDto toRestoreResultDto(List<UUID> restoredArticleIds) {
        return new ArticleRestoreResultDto(
                LocalDateTime.now(),
                restoredArticleIds,
                restoredArticleIds.size()
        );
    }

    public ArticleDto toHighlightedDto(ArticleQueryDto articleDto, List<String> keywords) {
        return new ArticleDto(
                articleDto.id(),
                articleDto.source(),
                articleDto.sourceUrl(),
                highlight(articleDto.title(), keywords),
                articleDto.publishDate(),
                highlight(articleDto.summary(), keywords),
                articleDto.commentCount(),
                articleDto.viewCount(),
                articleDto.viewedByMe()
        );
    }

    private String highlight(String text, List<String> keywords) {
        if (text == null || keywords == null || keywords.isEmpty()) {
            return text;
        }

        String result = text;
        for (String keyword : keywords) {
            if(keyword == null || keyword.isBlank()) continue;

            result = result.replaceAll(
                    Pattern.quote(keyword),
                    "<b>" + keyword + "</b>"
            );
        }
        return result;
    }

}
