package com.example.monew.domain.article.mapper;

import com.example.monew.domain.article.dto.ArticleDto;
import com.example.monew.domain.article.dto.ArticleRestoreResultDto;
import com.example.monew.domain.article.entity.Article;
import com.example.monew.domain.article.repository.ArticleViewRepository;
import com.example.monew.domain.comment.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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

}
