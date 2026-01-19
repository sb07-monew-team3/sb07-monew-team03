package com.example.monew.domain.activity.mapper;

import com.example.monew.domain.activity.dto.UserActivityCommentDto;
import com.example.monew.domain.activity.dto.UserActivityDto;
import com.example.monew.domain.comment.entity.Comment;
import com.example.monew.domain.comment.repository.CommentLikesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserActivityCommentMapper {
    private final CommentLikesRepository commentLikesRepository;
    public UserActivityCommentDto toDto(Comment comment){
        int likeCount= Math.toIntExact( commentLikesRepository.countByCommentId(comment.getId()));
        return new UserActivityCommentDto(
                comment.getId(),
                comment.getArticle().getId(),
                comment.getArticle().getTitle(),
                comment.getUser().getId(),
                comment.getUser().getNickName(),
                comment.getContent(),
                likeCount,
                comment.getCreatedAt()
        );
    }
}
