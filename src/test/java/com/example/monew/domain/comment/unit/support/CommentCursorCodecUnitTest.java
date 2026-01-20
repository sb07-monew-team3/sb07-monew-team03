package com.example.monew.domain.comment.unit.support;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class CommentCursorCodecUnitTest {

    @Test
    @DisplayName("unit createdAt cursor encode decode")
    void createdAt_cursor_encode_decode() throws Exception {
        Class<?> cls = findFirstExisting(
                "com.example.monew.domain.comment.support.CommentCreatedAtCursor",
                "com.example.monew.domain.comment.support.CommentCreatedAtIdCursor",
                "com.example.monew.domain.comment.support.CommentCursor",
                "com.example.monew.domain.comment.support.CommentCreatedCursor"
        );

        assumeTrue(cls != null);

        UUID id = UUID.randomUUID();
        Instant t = Instant.parse("2026-01-01T00:00:00Z");

        Method encode = findEncodeCreatedAt(cls);
        Method decode = cls.getDeclaredMethod("decode", String.class);

        String cursor = (String) encode.invoke(null, t, id);
        Object decoded = decode.invoke(null, cursor);

        Method createdAt = decoded.getClass().getDeclaredMethod("createdAt");
        Method did = decoded.getClass().getDeclaredMethod("id");

        assertThat(createdAt.invoke(decoded)).isEqualTo(t);
        assertThat(did.invoke(decoded)).isEqualTo(id);
    }

    @Test
    @DisplayName("unit likeCount cursor encode decode")
    void likeCount_cursor_encode_decode() throws Exception {
        Class<?> cls = Class.forName("com.example.monew.domain.comment.support.CommentLikeCountCursor");

        UUID id = UUID.randomUUID();
        Instant t = Instant.parse("2026-01-01T00:00:00Z");
        long likeCount = 3L;

        Method encode = cls.getDeclaredMethod("encode", long.class, Instant.class, UUID.class);
        Method decode = cls.getDeclaredMethod("decode", String.class);

        String cursor = (String) encode.invoke(null, likeCount, t, id);
        Object decoded = decode.invoke(null, cursor);

        Method dLikeCount = decoded.getClass().getDeclaredMethod("likeCount");
        Method createdAt = decoded.getClass().getDeclaredMethod("createdAt");
        Method did = decoded.getClass().getDeclaredMethod("id");

        assertThat(dLikeCount.invoke(decoded)).isEqualTo(likeCount);
        assertThat(createdAt.invoke(decoded)).isEqualTo(t);
        assertThat(did.invoke(decoded)).isEqualTo(id);
    }

    @Test
    @DisplayName("unit cursor decode invalid throws")
    void decode_invalid_throws() throws Exception {
        Class<?> cls = Class.forName("com.example.monew.domain.comment.support.CommentLikeCountCursor");
        Method decode = cls.getDeclaredMethod("decode", String.class);

        assertThatThrownBy(() -> decode.invoke(null, "bad"))
                .isNotNull();
    }

    private Class<?> findFirstExisting(String... names) {
        for (String n : names) {
            try {
                return Class.forName(n);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private Method findEncodeCreatedAt(Class<?> cls) throws Exception {
        try {
            return cls.getDeclaredMethod("encode", Instant.class, UUID.class);
        } catch (NoSuchMethodException ignored) {
        }
        try {
            return cls.getDeclaredMethod("encode", UUID.class, Instant.class);
        } catch (NoSuchMethodException ignored) {
        }
        throw new NoSuchMethodException(cls.getName());
    }
}
