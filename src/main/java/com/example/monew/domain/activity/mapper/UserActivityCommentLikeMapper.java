package com.example.monew.domain.activity.mapper;

import com.example.monew.domain.activity.dto.UserActivityCommentLikeDto;
import com.example.monew.domain.article.entity.Article;
import com.example.monew.domain.comment.entity.Comment;
import com.example.monew.domain.comment.entity.CommentLikes;
import com.example.monew.domain.comment.repository.CommentLikesRepository;
import com.example.monew.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserActivityCommentLikeMapper {

    public UserActivityCommentLikeDto toUserActivityCommentDto(CommentLikes commentLikes) {
        Comment comment = commentLikes.getComment();
        Article article = comment.getArticle();
        User commentUser = comment.getUser();
        return new UserActivityCommentLikeDto(
                commentLikes.getId(),
                commentLikes.getCreatedAt(),
                comment.getId(),
                article.getId(),
                article.getTitle(),
                commentUser.getId(),
                commentUser.getNickName(),
                comment.getContent(),
                0,
                comment.getCreatedAt()
        );
    }
}
