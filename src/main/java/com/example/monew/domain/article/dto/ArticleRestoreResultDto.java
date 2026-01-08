package com.example.monew.domain.article.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ArticleRestoreResultDto(
        LocalDateTime restoreDate,
        List<UUID> restoredArticleIds,
        Integer restoredArticleCount
) {}
