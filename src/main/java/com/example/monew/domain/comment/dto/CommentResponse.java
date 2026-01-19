package com.example.monew.domain.comment.dto;

import com.example.monew.domain.comment.entity.Comment;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class CommentResponse {

    private UUID id;
    private UUID articleId;
    private UUID userId;
    private String userNickname;
    private String content;
    private Instant createdAt;

    private long likeCount;
    private boolean likedByMe;

    public static CommentResponse from(Comment comment, long likeCount, boolean likedByMe) {
        return new CommentResponse(
                comment.getId(),
                comment.getArticle().getId(),
                comment.getUser().getId(),
                comment.getUser().getNickName(),
                comment.getContent(),
                comment.getCreatedAt(),
                likeCount,
                likedByMe
        );
    }
}
