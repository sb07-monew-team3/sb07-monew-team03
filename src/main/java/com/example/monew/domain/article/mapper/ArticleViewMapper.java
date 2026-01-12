package com.example.monew.domain.article.mapper;

import com.example.monew.domain.article.dto.ArticleViewDto;
import com.example.monew.domain.article.entity.Article;
import com.example.monew.domain.article.entity.ArticleView;
import com.example.monew.domain.article.repository.ArticleViewRepository;
import com.example.monew.domain.comment.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ArticleViewMapper {

    private final ArticleViewRepository articleViewRepository;
    private final CommentRepository commentRepository;

    public ArticleViewDto toResponseDto(ArticleView articleView) {

        Article article = articleView.getArticle();

        long commentCount = commentRepository.countByArticleId(article.getId());
        long articleViewCount = articleViewRepository.countByArticleId(article.getId());

        return new ArticleViewDto(
                articleView.getId(),
                articleView.getUser().getId(),
                articleView.getCreatedAt(),
                article.getId(),
                article.getSource(),
                article.getSourceUrl(),
                article.getTitle(),
                article.getPublishDate(),
                article.getSummary(),
                commentCount,
                articleViewCount
        );
    }
}
