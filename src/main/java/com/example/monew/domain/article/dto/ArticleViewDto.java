package com.example.monew.domain.article.dto;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record ArticleViewDto(
        UUID id,
        UUID viewedBy,
        Instant createdAt,
        UUID articleId,
        String source,
        String sourceUri,
        String articleTitle,
        LocalDateTime articlePublishedDate,
        String articleSummary,
        Integer articleCommentCount,
        Integer articleViewCount
) {}
