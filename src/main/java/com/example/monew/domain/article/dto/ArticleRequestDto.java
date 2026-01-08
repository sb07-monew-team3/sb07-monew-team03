package com.example.monew.domain.article.dto;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ArticleRequestDto(
        String keyword,
        UUID interestedId,
        List<Source> sourceIn,
        LocalDateTime publishDateFrom,
        LocalDateTime publishDateTo,
        Order orderBy,
        Direction direction,
        String cursor,
        Instant after,
        Integer limit
) {}
