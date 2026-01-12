package com.example.monew.domain.comment.support;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

public record CommentLikeCountCursor(long likeCount, Instant createdAt, UUID id) {

    public static String encode(long likeCount, Instant createdAt, UUID id) {
        String raw = likeCount + "|" + createdAt + "|" + id;
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    public static CommentLikeCountCursor decode(String cursor) {
        byte[] decoded = Base64.getUrlDecoder().decode(cursor);
        String raw = new String(decoded, StandardCharsets.UTF_8);

        String[] parts = raw.split("\\|");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid cursor format");
        }

        return new CommentLikeCountCursor(
                Long.parseLong(parts[0]),
                Instant.parse(parts[1]),
                UUID.fromString(parts[2])
        );
    }
}
