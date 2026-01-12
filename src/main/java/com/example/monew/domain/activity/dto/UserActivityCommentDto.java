package com.example.monew.domain.activity.dto;

import java.time.Instant;
import java.util.UUID;

public record UserActivityCommentDto(
        UUID id,
        UUID articleId,
        String articleTitle,
        UUID userId,
        String userNickname,
        String content,
        int likeCount,
        Instant createdAt

) {
}
