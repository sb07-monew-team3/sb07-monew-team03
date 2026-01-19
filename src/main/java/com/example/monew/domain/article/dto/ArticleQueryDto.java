package com.example.monew.domain.article.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ArticleQueryDto(
        UUID id,
        String source,
        String sourceUrl,
        String title,
        LocalDateTime publishDate,
        String summary,
        Long commentCount,
        Long viewCount,
        Boolean viewedByMe
) {}
