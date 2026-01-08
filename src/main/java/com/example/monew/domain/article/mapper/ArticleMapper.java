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

//        int commentCount = commentRepository.countByArticleId(article.getId());
        int commentCount = 1; // commentRepository 메서드 구현 해주시면 그때 변경하기
        int viewCount = articleViewRepository.countByArticleId(article.getId());

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
