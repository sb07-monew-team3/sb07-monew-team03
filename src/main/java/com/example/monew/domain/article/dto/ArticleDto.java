package com.example.monew.domain.article.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ArticleDto(
        UUID id,
        String source,
        String title,
        LocalDateTime publishDate,
        String summary,
        Integer commentCount,
        Integer viewCount,
        Boolean viewedByMe // 요청자의 조회 여부
) {}
