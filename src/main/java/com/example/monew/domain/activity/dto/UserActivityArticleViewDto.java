package com.example.monew.domain.activity.dto;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record UserActivityArticleViewDto(
        UUID id,
        UUID viewedBy,
        Instant createdAt,
        UUID articleId,
        String source,
        String sourceUrl,
        String articleTitle,
        LocalDateTime articlePublishedDate,
        String articleSummary,
        int articleCommentCount,
        int articleViewCount
) {
}
