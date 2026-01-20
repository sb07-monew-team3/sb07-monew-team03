package com.example.monew.domain.comment.unit.repository;

import com.example.monew.config.QuerydslTestConfig;
import com.example.monew.domain.article.entity.Article;
import com.example.monew.domain.comment.entity.Comment;
import com.example.monew.domain.comment.repository.CommentRepositoryImpl;
import com.example.monew.domain.user.entity.User;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({QuerydslTestConfig.class})
class CommentRepositoryImplDataJpaTest {

    @Autowired
    EntityManager em;

    @Autowired
    JPAQueryFactory queryFactory;

    @Test
    @DisplayName("createdAt DESC: 커서 없이 limit+1 조회")
    void findByArticleIdWithCursor_firstPage_desc() {
        CommentRepositoryImpl repo = new CommentRepositoryImpl(queryFactory);

        Article article = persistArticle(Instant.parse("2026-01-01T00:00:00Z"));

        persistComment(article, persistUser(Instant.parse("2026-01-01T00:00:00Z")), Instant.parse("2026-01-01T00:00:00Z"));
        persistComment(article, persistUser(Instant.parse("2026-01-01T00:00:00Z")), Instant.parse("2026-01-02T00:00:00Z"));
        persistComment(article, persistUser(Instant.parse("2026-01-01T00:00:00Z")), Instant.parse("2026-01-03T00:00:00Z"));

        em.flush();
        em.clear();

        List<Comment> res = repo.findByArticleIdWithCursor(
                article.getId(),
                null,
                null,
                true,
                3,
                Sort.Direction.DESC
        );

        assertThat(res).hasSizeLessThanOrEqualTo(3);
    }

    @Test
    @DisplayName("createdAt DESC: 커서 이후(after=true)로 다음 페이지 조회")
    void findByArticleIdWithCursor_nextPage_desc_afterTrue() {
        CommentRepositoryImpl repo = new CommentRepositoryImpl(queryFactory);

        Article article = persistArticle(Instant.parse("2026-01-01T00:00:00Z"));

        User u1 = persistUser(Instant.parse("2026-01-01T00:00:00Z"));
        User u2 = persistUser(Instant.parse("2026-01-01T00:00:00Z"));

        Comment c1 = persistComment(article, u1, Instant.parse("2026-01-03T00:00:00Z"));
        Comment c2 = persistComment(article, u2, Instant.parse("2026-01-02T00:00:00Z"));
        Comment c3 = persistComment(article, u1, Instant.parse("2026-01-01T00:00:00Z"));

        em.flush();
        em.clear();

        List<Comment> res = repo.findByArticleIdWithCursor(
                article.getId(),
                c2.getCreatedAt(),
                c2.getId(),
                true,
                10,
                Sort.Direction.DESC
        );

        assertThat(res).allSatisfy(c -> assertThat(c.getCreatedAt()).isBeforeOrEqualTo(c2.getCreatedAt()));
    }

    @Test
    @DisplayName("createdAt ASC: 커서 이후(after=true)로 다음 페이지 조회")
    void findByArticleIdWithCursor_nextPage_asc_afterTrue() {
        CommentRepositoryImpl repo = new CommentRepositoryImpl(queryFactory);

        Article article = persistArticle(Instant.parse("2026-01-01T00:00:00Z"));

        User u = persistUser(Instant.parse("2026-01-01T00:00:00Z"));

        Comment c1 = persistComment(article, u, Instant.parse("2026-01-01T00:00:00Z"));
        Comment c2 = persistComment(article, u, Instant.parse("2026-01-02T00:00:00Z"));
        Comment c3 = persistComment(article, u, Instant.parse("2026-01-03T00:00:00Z"));

        em.flush();
        em.clear();

        List<Comment> res = repo.findByArticleIdWithCursor(
                article.getId(),
                c2.getCreatedAt(),
                c2.getId(),
                true,
                10,
                Sort.Direction.ASC
        );

        assertThat(res).allSatisfy(c -> assertThat(c.getCreatedAt()).isAfterOrEqualTo(c2.getCreatedAt()));
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
}
