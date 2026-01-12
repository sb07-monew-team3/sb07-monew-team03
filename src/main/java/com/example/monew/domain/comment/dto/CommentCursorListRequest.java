package com.example.monew.domain.comment.dto;

import org.springframework.data.domain.Sort;

import java.util.UUID;

public record CommentCursorListRequest(
        UUID articleId,
        String orderBy,
        Sort.Direction direction,
        String cursor,
        Boolean after,
        Integer limit
) {
    public String resolvedOrderBy() {
        return (orderBy == null || orderBy.isBlank()) ? "createdAt" : orderBy;
    }

    public Sort.Direction resolvedDirection() {
        return direction == null ? Sort.Direction.DESC : direction;
    }

    public boolean resolvedAfter() {
        return after == null || after;
    }

    public int resolvedLimit() {
        if (limit == null) return 20;
        if (limit < 1) return 1;
        return Math.min(limit, 50);
    }
}
