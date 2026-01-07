package com.example.monew.domain.comment.dto;

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
    private String content;
    private Instant createdAt;

    private long likeCount;
    private boolean likedByMe;
}
