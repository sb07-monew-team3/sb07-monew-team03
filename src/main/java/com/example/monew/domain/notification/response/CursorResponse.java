package com.example.monew.domain.notification.response;

import java.time.Instant;
import java.util.List;

public record CursorResponse<T>(
   List<T> content,
   String nextCursor, // 마지막 요소의 ID
   Instant nextAfter, // 마지막 요소의 createdAt
   int size,
   Long totalElements,
   boolean hasNext
) {
}
