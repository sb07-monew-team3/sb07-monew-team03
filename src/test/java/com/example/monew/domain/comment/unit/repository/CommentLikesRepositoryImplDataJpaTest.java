package com.example.monew.domain.comment.unit.repository;

import com.example.monew.config.QuerydslTestConfig;
import com.example.monew.domain.article.entity.Article;
import com.example.monew.domain.comment.entity.Comment;
import com.example.monew.domain.comment.entity.CommentLikes;
import com.example.monew.domain.comment.repository.CommentLikesRepositoryImpl;
import com.example.monew.domain.user.entity.User;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({QuerydslTestConfig.class})
class CommentLikesRepositoryImplDataJpaTest {

    @Autowired
    EntityManager em;

    @Autowired
    JPAQueryFactory queryFactory;

    @Test
    @DisplayName("좋아요 커스텀 조회/집계 메서드들 커버")
    void custom_methods() {
        CommentLikesRepositoryImpl repo = new CommentLikesRepositoryImpl(queryFactory);

        Instant base = Instant.parse("2026-01-01T00:00:00Z");
        Article article = persistArticle(base);

        User u1 = persistUser(base);
        User u2 = persistUser(base);

        Comment c1 = persistComment(article, u1, Instant.parse("2026-01-01T00:00:00Z"));
        Comment c2 = persistComment(article, u1, Instant.parse("2026-01-02T00:00:00Z"));

        persistLike(u1, c1, base.plusSeconds(1));
        persistLike(u2, c1, base.plusSeconds(2));
        persistLike(u2, c2, base.plusSeconds(3));

        em.flush();
        em.clear();

        List<CommentLikes> byUser = repo.getCommentLikesByUserId(u2.getId());
        assertThat(byUser).isNotNull();

        Long countOne = repo.countByCommentId(c1.getId());
        assertThat(countOne).isGreaterThanOrEqualTo(1L);

        Map<UUID, Long> countMap = repo.countByCommentIds(List.of(c1.getId(), c2.getId()));
        assertThat(countMap).containsKeys(c1.getId(), c2.getId());

        Set<UUID> likedIds = repo.findLikedCommentIds(u2.getId(), List.of(c1.getId(), c2.getId()));
        assertThat(likedIds).contains(c1.getId(), c2.getId());
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
