package com.example.monew.domain.comment.unit.service;

import com.example.monew.domain.article.entity.Article;
import com.example.monew.domain.comment.dto.*;
import com.example.monew.domain.comment.entity.Comment;
import com.example.monew.domain.comment.repository.CommentLikesRepository;
import com.example.monew.domain.comment.repository.CommentRepository;
import com.example.monew.domain.comment.repository.CommentWithLikeCount;
import com.example.monew.domain.comment.service.CommentQueryService;
import com.example.monew.domain.comment.support.CommentCursor;
import com.example.monew.domain.comment.support.CommentLikeCountCursor;
import com.example.monew.domain.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentQueryServiceTest {

    @Mock
    CommentRepository commentRepository;

    @Mock
    CommentLikesRepository commentLikesRepository;

    @InjectMocks
    CommentQueryService commentQueryService;

    @Test
    @DisplayName("getCommentsByCursor: createdAt")
    void getCommentsByCursor_createdAt() {
        UUID articleId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();

        Article article = mock(Article.class);
        when(article.getId()).thenReturn(articleId);

        User user = mock(User.class);
        when(user.getId()).thenReturn(UUID.randomUUID());

        Comment c1 = new Comment(user, article, "c1", false);
        Comment c2 = new Comment(user, article, "c2", false);

        UUID c1Id = UUID.randomUUID();
        UUID c2Id = UUID.randomUUID();
        ReflectionTestUtils.setField(c1, "id", c1Id);
        ReflectionTestUtils.setField(c2, "id", c2Id);

        Instant t1 = Instant.parse("2026-01-01T00:00:00Z");
        Instant t2 = Instant.parse("2026-01-02T00:00:00Z");
        ReflectionTestUtils.setField(c1, "createdAt", t1);
        ReflectionTestUtils.setField(c2, "createdAt", t2);

        List<Comment> fetched = List.of(c2, c1, c1);

        when(commentRepository.findByArticleIdWithCursor(
                eq(articleId),
                any(),
                any(),
                eq(true),
                eq(3),
                eq(Sort.Direction.DESC)
        )).thenReturn(fetched);

        when(commentLikesRepository.countByCommentIds(anyList())).thenReturn(Map.of());
        when(commentLikesRepository.findLikedCommentIds(eq(requesterId), anyList())).thenReturn(Set.of());

        CommentCursorListRequest req = new CommentCursorListRequest(
                articleId,
                "createdAt",
                Sort.Direction.DESC,
                null,
                true,
                2
        );

        CursorPageResponse<CommentResponse> res = commentQueryService.getCommentsByCursor(requesterId, req);

        assertThat(res.items()).hasSize(2);
        assertThat(res.hasNext()).isTrue();
    }

    @Test
    @DisplayName("getCommentsByCursor: likeCount")
    void getCommentsByCursor_likeCount() {
        UUID articleId = UUID.randomUUID();

        Article article = mock(Article.class);
        when(article.getId()).thenReturn(articleId);

        User user = mock(User.class);
        when(user.getId()).thenReturn(UUID.randomUUID());

        Comment comment = new Comment(user, article, "c", false);
        UUID commentId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-01-02T00:00:00Z");

        ReflectionTestUtils.setField(comment, "id", commentId);
        ReflectionTestUtils.setField(comment, "createdAt", createdAt);

        CommentWithLikeCount row = mock(CommentWithLikeCount.class);
        when(row.comment()).thenReturn(comment);
        when(row.likeCount()).thenReturn(10L);

        when(commentRepository.findByArticleIdOrderByLikeCountWithCursor(
                eq(articleId),
                any(),
                any(),
                any(),
                eq(true),
                eq(3),
                eq(Sort.Direction.DESC)
        )).thenReturn(List.of(row, row, row));

        when(commentLikesRepository.findLikedCommentIds(isNull(), anyList())).thenReturn(Set.of());

        String cursor = CommentLikeCountCursor.encode(10L, createdAt, commentId);

        CommentCursorListRequest req = new CommentCursorListRequest(
                articleId,
                "likeCount",
                Sort.Direction.DESC,
                cursor,
                true,
                2
        );

        CursorPageResponse<CommentResponse> res = commentQueryService.getCommentsByCursor(null, req);

        assertThat(res.items()).hasSize(2);
        assertThat(res.hasNext()).isTrue();
    }

    @Test
    @DisplayName("getCommentsForFront: createdAt cursor 파싱 성공")
    void getCommentsForFront_createdAt_cursorParsed() {
        UUID requesterId = UUID.randomUUID();
        UUID articleId = UUID.randomUUID();

        Article article = mock(Article.class);
        when(article.getId()).thenReturn(articleId);

        User user = mock(User.class);
        when(user.getId()).thenReturn(UUID.randomUUID());

        Instant cursor = Instant.parse("2026-01-03T00:00:00Z");

        Comment comment = new Comment(user, article, "c", false);
        ReflectionTestUtils.setField(comment, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(comment, "createdAt", cursor);

        when(commentRepository.findByArticleIdWithCreatedAtCursorOnly(
                eq(articleId),
                eq(cursor),
                eq(true),
                eq(6),
                eq(Sort.Direction.DESC)
        )).thenReturn(List.of(comment));

        when(commentLikesRepository.countByCommentIds(anyList())).thenReturn(Map.of());
        when(commentLikesRepository.findLikedCommentIds(eq(requesterId), anyList())).thenReturn(Set.of());

        when(commentRepository.countByArticleId(articleId)).thenReturn(1L);

        CommentCursorPageResponse<CommentResponse> res = commentQueryService.getCommentsForFront(
                requesterId,
                articleId,
                "createdAt",
                Sort.Direction.DESC,
                cursor.toString(),
                true,
                5
        );

        assertThat(res.content()).hasSize(1);
    }

    @Test
    @DisplayName("getCommentsForFront: likeCount 분기(강제 limit/hasNext=false) 커버")
    void getCommentsForFront_likeCount_branch() {
        UUID requesterId = UUID.randomUUID();
        UUID articleId = UUID.randomUUID();

        when(commentRepository.countByArticleId(articleId)).thenReturn(2L);

        Article article = mock(Article.class);
        when(article.getId()).thenReturn(articleId);

        User user = mock(User.class);
        when(user.getId()).thenReturn(UUID.randomUUID());

        Comment c1 = new Comment(user, article, "c1", false);
        Comment c2 = new Comment(user, article, "c2", false);
        ReflectionTestUtils.setField(c1, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(c2, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(c1, "createdAt", Instant.parse("2026-01-01T00:00:00Z"));
        ReflectionTestUtils.setField(c2, "createdAt", Instant.parse("2026-01-02T00:00:00Z"));

        CommentWithLikeCount r1 = mock(CommentWithLikeCount.class);
        when(r1.comment()).thenReturn(c1);
        when(r1.likeCount()).thenReturn(5L);

        CommentWithLikeCount r2 = mock(CommentWithLikeCount.class);
        when(r2.comment()).thenReturn(c2);
        when(r2.likeCount()).thenReturn(1L);

        when(commentRepository.findByArticleIdOrderByLikeCountWithCreatedAtCursorOnly(
                eq(articleId),
                isNull(),
                eq(true),
                eq(3),
                eq(Sort.Direction.DESC)
        )).thenReturn(List.of(r1, r2));

        when(commentLikesRepository.findLikedCommentIds(eq(requesterId), anyList())).thenReturn(Set.of());

        CommentCursorPageResponse<CommentResponse> res = commentQueryService.getCommentsForFront(
                requesterId,
                articleId,
                "likeCount",
                Sort.Direction.DESC,
                null,
                true,
                5
        );

        assertThat(res.content()).hasSize(2);
        assertThat(res.hasNext()).isFalse();
        assertThat(res.nextCursor()).isNull();
        assertThat(res.nextAfter()).isNull();
        assertThat(res.totalElements()).isEqualTo(2L);
    }

    @Test
    @DisplayName("getCommentsForFront: invalid cursor면 예외")
    void getCommentsForFront_invalidCursor() {
        assertThatThrownBy(() -> commentQueryService.getCommentsForFront(
                null,
                UUID.randomUUID(),
                "createdAt",
                Sort.Direction.DESC,
                "invalid",
                true,
                5
        )).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("getCommentsByCursor: invalid cursor면 예외")
    void getCommentsByCursor_invalidCursor() {
        CommentCursorListRequest req = new CommentCursorListRequest(
                UUID.randomUUID(),
                "createdAt",
                Sort.Direction.DESC,
                "not-base64",
                true,
                2
        );

        assertThatThrownBy(() -> commentQueryService.getCommentsByCursor(null, req))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("getCommentsByCursor: cursor decode 정상 경로 (createdAt)")
    void getCommentsByCursor_createdAt_withCursor() {
        UUID articleId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();

        Article article = mock(Article.class);
        when(article.getId()).thenReturn(articleId);

        User user = mock(User.class);
        when(user.getId()).thenReturn(UUID.randomUUID());

        Comment last = new Comment(user, article, "last", false);
        UUID lastId = UUID.randomUUID();
        Instant lastAt = Instant.parse("2026-01-04T00:00:00Z");
        ReflectionTestUtils.setField(last, "id", lastId);
        ReflectionTestUtils.setField(last, "createdAt", lastAt);

        String cursor = CommentCursor.encode(lastAt, lastId);

        when(commentRepository.findByArticleIdWithCursor(
                eq(articleId),
                any(),
                any(),
                eq(true),
                eq(3),
                eq(Sort.Direction.DESC)
        )).thenReturn(List.of(last, last, last));

        when(commentLikesRepository.countByCommentIds(anyList())).thenReturn(Map.of());
        when(commentLikesRepository.findLikedCommentIds(eq(requesterId), anyList())).thenReturn(Set.of());

        CommentCursorListRequest req = new CommentCursorListRequest(
                articleId,
                "createdAt",
                Sort.Direction.DESC,
                cursor,
                true,
                2
        );

        CursorPageResponse<CommentResponse> res = commentQueryService.getCommentsByCursor(requesterId, req);

        assertThat(res.items()).hasSize(2);
        assertThat(res.hasNext()).isTrue();
    }

    @Test
    @DisplayName("getCommentsByCursor(req) 오버로드 메서드 커버")
    void getCommentsByCursor_overload() {
        UUID articleId = UUID.randomUUID();

        CommentCursorListRequest req = new CommentCursorListRequest(
                articleId,
                "createdAt",
                Sort.Direction.DESC,
                null,
                true,
                1
        );

        Article article = mock(Article.class);
        when(article.getId()).thenReturn(articleId);

        User user = mock(User.class);
        when(user.getId()).thenReturn(UUID.randomUUID());

        Comment c = new Comment(user, article, "c", false);
        ReflectionTestUtils.setField(c, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(c, "createdAt", Instant.parse("2026-01-01T00:00:00Z"));

        when(commentRepository.findByArticleIdWithCursor(eq(articleId), any(), any(), eq(true), eq(2), eq(Sort.Direction.DESC)))
                .thenReturn(List.of(c, c));

        when(commentLikesRepository.countByCommentIds(anyList())).thenReturn(Map.of());
        when(commentLikesRepository.findLikedCommentIds(isNull(), anyList())).thenReturn(Set.of());

        CursorPageResponse<CommentResponse> res = commentQueryService.getCommentsByCursor(req);

        assertThat(res.items()).hasSize(1);
        assertThat(res.hasNext()).isTrue();
    }
}
