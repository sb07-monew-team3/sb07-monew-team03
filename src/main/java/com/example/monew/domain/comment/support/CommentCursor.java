package com.example.monew.domain.comment.support;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

public record CommentCursor(Instant createdAt, UUID id) {

    public static String encode(Instant createdAt, UUID id) {
        String raw = createdAt.toString() + "|" + id;
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    public static CommentCursor decode(String cursor) {
        byte[] decoded = Base64.getUrlDecoder().decode(cursor);
        String raw = new String(decoded, StandardCharsets.UTF_8);

        String[] parts = raw.split("\\|");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid cursor format");
        }

        return new CommentCursor(
                Instant.parse(parts[0]),
                UUID.fromString(parts[1])
        );
    }
}
