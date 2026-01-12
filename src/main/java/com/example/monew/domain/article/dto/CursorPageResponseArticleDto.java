package com.example.monew.domain.article.dto;

import java.time.Instant;
import java.util.List;

public record CursorPageResponseArticleDto (
        List<ArticleDto> content,
        String nextCursor,
        Instant nextAfter,
        int size,
        long totalElements,
        boolean hasNext
) {}
