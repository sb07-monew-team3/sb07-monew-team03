package com.example.monew.domain.article.mapper;

import com.example.monew.domain.article.dto.ArticleDto;
import com.example.monew.domain.article.entity.Article;
import com.example.monew.domain.article.repository.ArticleViewRepository;
import com.example.monew.domain.comment.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ArticleMapper {
    private final ArticleViewRepository articleViewRepository;
    private final CommentRepository commentRepository;

    public ArticleDto toDto(Article article, UUID userId) {
        boolean viewedByMe = articleViewRepository.existsByArticleIdAndUserId(article.getId(), userId);

        long commentCount = commentRepository.countByArticleId(article.getId());
        long viewCount = articleViewRepository.countByArticleId(article.getId());

        return new ArticleDto(
                article.getId(),
                article.getSource(),
                article.getTitle(),
                article.getPublishDate(),
                article.getSummary(),
                commentCount,
                viewCount,
                viewedByMe
        );
    }

}
