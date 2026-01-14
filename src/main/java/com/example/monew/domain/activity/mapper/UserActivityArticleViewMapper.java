package com.example.monew.domain.activity.mapper;

import com.example.monew.domain.activity.dto.UserActivityArticleViewDto;
import com.example.monew.domain.article.entity.Article;
import com.example.monew.domain.article.entity.ArticleView;
import com.example.monew.domain.article.repository.ArticleViewRepository;
import com.example.monew.domain.comment.entity.Comment;
import com.example.monew.domain.comment.repository.CommentRepository;
import com.example.monew.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UserActivityArticleViewMapper {

    private final CommentRepository commentRepository;
    private final ArticleViewRepository articleViewRepository;

    public UserActivityArticleViewDto toDto(ArticleView articleView){

        Article article = articleView.getArticle();
        User user = articleView.getUser();
        long validCommentCount = commentRepository.findByArticle_Id(article.getId()).stream()
                .filter(x -> !x.isDeleted())
                .count();

        int articleCommentCount = Math.toIntExact(validCommentCount);
        int articleViewCount = Math.toIntExact(articleViewRepository.countByArticleId(article.getId()));


        return new UserActivityArticleViewDto(
                articleView.getId(),
                user.getId(),
                articleView.getCreatedAt(),
                article.getId(),
                article.getSource(),
                article.getSourceUrl(),
                article.getTitle(),
                article.getPublishDate(),
                article.getSummary(),
                articleCommentCount,
                articleViewCount
        );

    }
}
