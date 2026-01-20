package com.example.monew.domain.comment.unit.repository;

import com.example.monew.domain.article.entity.Article;
import com.example.monew.domain.comment.entity.Comment;
import com.example.monew.domain.comment.entity.CommentLikes;
import com.example.monew.domain.comment.repository.CommentRepositoryImpl;
import com.example.monew.domain.comment.repository.CommentWithLikeCount;
import com.example.monew.domain.user.entity.User;
import com.example.monew.global.config.QueryDslConfig;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({QueryDslConfig.class})
class CommentRepositoryImplLikeCountDataJpaTest {

    @Autowired
    EntityManager em;

    @Autowired
    JPAQueryFactory queryFactory;

    @Test
    @DisplayName("likeCount 정렬 Page 조회")
    void findByArticleIdOrderByLikeCount() {
        CommentRepositoryImpl repo = new CommentRepositoryImpl(queryFactory);

        Instant base = Instant.parse("2026-01-01T00:00:00Z");
        Article article = persistArticle(base);

        User u1 = persistUser(base);
        User u2 = persistUser(base);
        User u3 = persistUser(base);

        Comment c1 = persistComment(article, u1, Instant.parse("2026-01-01T00:00:00Z"));
        Comment c2 = persistComment(article, u2, Instant.parse("2026-01-02T00:00:00Z"));
        Comment c3 = persistComment(article, u3, Instant.parse("2026-01-03T00:00:00Z"));

        persistLike(u1, c2, Instant.parse("2026-01-04T00:00:00Z"));
        persistLike(u2, c2, Instant.parse("2026-01-05T00:00:00Z"));
        persistLike(u1, c3, Instant.parse("2026-01-06T00:00:00Z"));

        em.flush();
        em.clear();

        var pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "likeCount"));

        var page = repo.findByArticleIdOrderByLikeCount(article.getId(), pageable, Sort.Direction.DESC);

        assertThat(page.getContent()).isNotEmpty();
        assertThat(page.getContent().get(0).likeCount()).isGreaterThanOrEqualTo(page.getContent().get(page.getContent().size() - 1).likeCount());
    }

    @Test
    @DisplayName("likeCount + createdAt 커서-only 조회")
    void findByArticleIdOrderByLikeCountWithCreatedAtCursorOnly() {
        CommentRepositoryImpl repo = new CommentRepositoryImpl(queryFactory);

        Instant base = Instant.parse("2026-01-01T00:00:00Z");
        Article article = persistArticle(base);

        User u1 = persistUser(base);
        User u2 = persistUser(base);
        User u3 = persistUser(base);

        Comment c1 = persistComment(article, u1, Instant.parse("2026-01-01T00:00:00Z"));
        Comment c2 = persistComment(article, u2, Instant.parse("2026-01-02T00:00:00Z"));
        Comment c3 = persistComment(article, u3, Instant.parse("2026-01-03T00:00:00Z"));

        persistLike(u1, c2, Instant.parse("2026-01-04T00:00:00Z"));
        persistLike(u2, c2, Instant.parse("2026-01-05T00:00:00Z"));
        persistLike(u1, c3, Instant.parse("2026-01-06T00:00:00Z"));

        em.flush();
        em.clear();

        List<CommentWithLikeCount> res = repo.findByArticleIdOrderByLikeCountWithCreatedAtCursorOnly(
                article.getId(),
                Instant.parse("2026-01-03T00:00:00Z"),
                true,
                10,
                Sort.Direction.DESC
        );

        assertThat(res).isNotNull();
    }

    @Test
    @DisplayName("likeCount 커서(좋아요수+createdAt+id) 조회")
    void findByArticleIdOrderByLikeCountWithCursor() {
        CommentRepositoryImpl repo = new CommentRepositoryImpl(queryFactory);

        Instant base = Instant.parse("2026-01-01T00:00:00Z");
        Article article = persistArticle(base);

        User u1 = persistUser(base);
        User u2 = persistUser(base);
        User u3 = persistUser(base);

        Comment c1 = persistComment(article, u1, Instant.parse("2026-01-01T00:00:00Z"));
        Comment c2 = persistComment(article, u2, Instant.parse("2026-01-02T00:00:00Z"));
        Comment c3 = persistComment(article, u3, Instant.parse("2026-01-03T00:00:00Z"));

        persistLike(u1, c2, Instant.parse("2026-01-04T00:00:00Z"));
        persistLike(u2, c2, Instant.parse("2026-01-05T00:00:00Z"));
        persistLike(u1, c3, Instant.parse("2026-01-06T00:00:00Z"));

        em.flush();
        em.clear();

        List<CommentWithLikeCount> first = repo.findByArticleIdOrderByLikeCountWithCursor(
                article.getId(),
                null,
                null,
                null,
                true,
                10,
                Sort.Direction.DESC
        );

        assertThat(first).isNotEmpty();

        CommentWithLikeCount cursorRow = first.get(0);

        List<CommentWithLikeCount> next = repo.findByArticleIdOrderByLikeCountWithCursor(
                article.getId(),
                cursorRow.likeCount(),
                cursorRow.comment().getCreatedAt(),
                cursorRow.comment().getId(),
                true,
                10,
                Sort.Direction.DESC
        );

        assertThat(next).isNotNull();
    }

    @Test
    @DisplayName("createdAt 커서-only 조회 + countByArticleId + getCommentsByUserId")
    void createdAtCursorOnly_and_counts() {
        CommentRepositoryImpl repo = new CommentRepositoryImpl(queryFactory);

        Instant base = Instant.parse("2026-01-01T00:00:00Z");
        Article article = persistArticle(base);

        User u1 = persistUser(base);
        User u2 = persistUser(base);

        persistComment(article, u1, Instant.parse("2026-01-01T00:00:00Z"));
        persistComment(article, u1, Instant.parse("2026-01-02T00:00:00Z"));
        persistComment(article, u2, Instant.parse("2026-01-03T00:00:00Z"));

        em.flush();
        em.clear();

        List<Comment> res = repo.findByArticleIdWithCreatedAtCursorOnly(
                article.getId(),
                Instant.parse("2026-01-03T00:00:00Z"),
                true,
                10,
                Sort.Direction.DESC
        );

        assertThat(res).isNotNull();

        Long cnt = repo.countByArticleId(article.getId());
        assertThat(cnt).isGreaterThanOrEqualTo(3L);

        List<Comment> byUser = repo.getCommentsByUserId(u1.getId());
        assertThat(byUser).isNotNull();
    }

    private User persistUser(Instant createdAt) {
        User user = new User(
                "e" + UUID.randomUUID() + "@t.com",
                "nick" + UUID.randomUUID(),
                "pw",
                Instant.now()
        );
        ReflectionTestUtils.setField(user, "createdAt", createdAt);
        em.persist(user);
        return user;
    }

    private Article persistArticle(Instant createdAt) {
        Article article = new Article(
                "title",
                "content",
                "NAVER",
                LocalDateTime.now(),
                "http://example.com/" + UUID.randomUUID(),
                false,
                Instant.now(),
                List.of()
        );
        ReflectionTestUtils.setField(article, "createdAt", createdAt);
        ReflectionTestUtils.setField(article, "sortTimestamp", createdAt);
        try {
            ReflectionTestUtils.setField(article, "publishDate", LocalDateTime.now());
        } catch (Exception ignored) {
        }
        em.persist(article);
        return article;
    }

    private Comment persistComment(Article article, User user, Instant createdAt) {
        Comment comment = new Comment(user, article, "c", false);
        ReflectionTestUtils.setField(comment, "createdAt", createdAt);
        em.persist(comment);
        return comment;
    }

    private CommentLikes persistLike(User user, Comment comment, Instant createdAt) {
        CommentLikes likes = new CommentLikes(user, comment);
        ReflectionTestUtils.setField(likes, "createdAt", createdAt);
        em.persist(likes);
        return likes;
    }
}
