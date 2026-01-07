package com.example.monew.domain.comment.dto;

import java.time.Instant;
import java.util.UUID;

public record CommentResponse(
        UUID id,
        UUID articleId,
        UUID userId,
        String content,
        Instant createdAt,
        long likeCount,
        boolean likedByMe
) {
}
//commit