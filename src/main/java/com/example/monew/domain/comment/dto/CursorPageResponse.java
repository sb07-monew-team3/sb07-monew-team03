package com.example.monew.domain.comment.dto;

import java.util.List;

public record CursorPageResponse<T>(
        List<T> items,
        String nextCursor,
        boolean hasNext
) {
    public static <T> CursorPageResponse<T> of(List<T> items, String nextCursor, boolean hasNext) {
        return new CursorPageResponse<>(items, nextCursor, hasNext);
    }
}
