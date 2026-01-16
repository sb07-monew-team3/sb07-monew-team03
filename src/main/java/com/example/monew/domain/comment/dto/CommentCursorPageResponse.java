package com.example.monew.domain.comment.dto;

import java.util.List;

public record CommentCursorPageResponse<T>(
        List<T> content,
        String nextCursor,
        String nextAfter,
        int size,
        long totalElements,
        boolean hasNext
) {
    public static <T> CommentCursorPageResponse<T> of(
            List<T> content,
            String nextCursor,
            String nextAfter,
            int size,
            long totalElements,
            boolean hasNext
    ) {
        return new CommentCursorPageResponse<>(content, nextCursor, nextAfter, size, totalElements, hasNext);
    }
}
