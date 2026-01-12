package com.example.monew.domain.comment.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "CLIENT_ID=test-client-id",
        "CLIENT_SECRET=test-client-secret"
})
@AutoConfigureMockMvc
class CommentCursorLikeCountIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired JdbcTemplate jdbcTemplate;

    private final List<UUID> createdLikeIds = new ArrayList<>();
    private final List<UUID> createdCommentIds = new ArrayList<>();
    private final List<UUID> createdArticleIds = new ArrayList<>();
    private final List<UUID> createdUserIds = new ArrayList<>();

    @AfterEach
    void tearDown() {
        for (UUID likeId : createdLikeIds) {
            jdbcTemplate.update("DELETE FROM comment_likes WHERE id = ?", likeId);
        }
        for (UUID commentId : createdCommentIds) {
            jdbcTemplate.update("DELETE FROM comments WHERE id = ?", commentId);
        }
        for (UUID articleId : createdArticleIds) {
            jdbcTemplate.update("DELETE FROM articles WHERE id = ?", articleId);
        }
        for (UUID userId : createdUserIds) {
            jdbcTemplate.update("DELETE FROM users WHERE id = ?", userId);
        }
    }

    @Test
    @DisplayName("likeCount 정렬 첫 페이지: DESC 기준으로 좋아요 많은 댓글이 먼저 온다")
    void likeCount_firstPage_desc_ordering() throws Exception {
        UUID writerId = createUser("writer");
        UUID articleId = createArticle();

        UUID c1 = createComment(articleId, writerId, Instant.parse("2026-01-12T00:00:01Z"), "c1"); // like 3
        UUID c2 = createComment(articleId, writerId, Instant.parse("2026-01-12T00:00:04Z"), "c2"); // like 2 (newer)
        UUID c3 = createComment(articleId, writerId, Instant.parse("2026-01-12T00:00:03Z"), "c3"); // like 2 (older than c2)
        UUID c4 = createComment(articleId, writerId, Instant.parse("2026-01-12T00:00:02Z"), "c4"); // like 1

        addLikes(c1, 3);
        addLikes(c2, 2);
        addLikes(c3, 2);
        addLikes(c4, 1);

        String json = mockMvc.perform(
                        get("/api/comments/cursor")
                                .param("articleId", articleId.toString())
                                .param("orderBy", "likeCount")
                                .param("direction", "DESC")
                                .param("limit", "2")
                        // likedByMe 확인하려면 헤더를 넣어도 되지만, 이 테스트는 정렬이 목적
                )
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode root = objectMapper.readTree(json);

        assertThat(root.get("items").size()).isEqualTo(2);
        assertThat(root.get("hasNext").asBoolean()).isTrue();
        assertThat(root.get("nextCursor").asText()).isNotBlank();

        // 1등은 likeCount=3인 c1
        UUID firstId = UUID.fromString(root.get("items").get(0).get("id").asText());
        long firstLikeCount = root.get("items").get(0).get("likeCount").asLong();
        assertThat(firstId).isEqualTo(c1);
        assertThat(firstLikeCount).isEqualTo(3L);

        // 2등은 likeCount=2 (c2 또는 c3)인데, tie-breaker가 createdAt DESC면 더 "최신"인 c2가 먼저 와야 함
        UUID secondId = UUID.fromString(root.get("items").get(1).get("id").asText());
        long secondLikeCount = root.get("items").get(1).get("likeCount").asLong();
        assertThat(secondLikeCount).isEqualTo(2L);
        assertThat(secondId).isEqualTo(c2);
    }

    @Test
    @DisplayName("likeCount 정렬 다음 페이지: nextCursor로 재호출하면 중복 없이 이어지고 tie(같은 likeCount)도 누락되지 않는다")
    void likeCount_nextPage_noDuplicate_noMissing() throws Exception {
        UUID writerId = createUser("writer");
        UUID articleId = createArticle();

        UUID c1 = createComment(articleId, writerId, Instant.parse("2026-01-12T00:00:01Z"), "c1"); // like 3
        UUID c2 = createComment(articleId, writerId, Instant.parse("2026-01-12T00:00:04Z"), "c2"); // like 2 (newer)
        UUID c3 = createComment(articleId, writerId, Instant.parse("2026-01-12T00:00:03Z"), "c3"); // like 2 (older)
        UUID c4 = createComment(articleId, writerId, Instant.parse("2026-01-12T00:00:02Z"), "c4"); // like 1

        addLikes(c1, 3);
        addLikes(c2, 2);
        addLikes(c3, 2);
        addLikes(c4, 1);

        // 1페이지(limit=2)
        String json1 = mockMvc.perform(
                        get("/api/comments/cursor")
                                .param("articleId", articleId.toString())
                                .param("orderBy", "likeCount")
                                .param("direction", "DESC")
                                .param("limit", "2")
                )
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode root1 = objectMapper.readTree(json1);
        String nextCursor = root1.get("nextCursor").asText();
        assertThat(nextCursor).isNotBlank();

        UUID p1_1 = UUID.fromString(root1.get("items").get(0).get("id").asText());
        UUID p1_2 = UUID.fromString(root1.get("items").get(1).get("id").asText());

        // 2페이지(nextCursor)
        String json2 = mockMvc.perform(
                        get("/api/comments/cursor")
                                .param("articleId", articleId.toString())
                                .param("orderBy", "likeCount")
                                .param("direction", "DESC")
                                .param("limit", "2")
                                .param("cursor", nextCursor)
                )
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode root2 = objectMapper.readTree(json2);

        assertThat(root2.get("items").size()).isEqualTo(2);
        assertThat(root2.get("hasNext").asBoolean()).isFalse();

        UUID p2_1 = UUID.fromString(root2.get("items").get(0).get("id").asText());
        UUID p2_2 = UUID.fromString(root2.get("items").get(1).get("id").asText());

        // 전체 4개가 중복 없이 모두 등장해야 함
        Set<UUID> all = new HashSet<>(List.of(p1_1, p1_2, p2_1, p2_2));
        assertThat(all).hasSize(4);
        assertThat(all).contains(c1, c2, c3, c4);

        // 2페이지는 남은 likeCount=2(=c3) + likeCount=1(=c4) 순서여야 함
        long page2_firstLikeCount = root2.get("items").get(0).get("likeCount").asLong();
        long page2_secondLikeCount = root2.get("items").get(1).get("likeCount").asLong();
        assertThat(page2_firstLikeCount).isEqualTo(2L);
        assertThat(page2_secondLikeCount).isEqualTo(1L);

        assertThat(p2_1).isEqualTo(c3);
        assertThat(p2_2).isEqualTo(c4);
    }

    // -------------------------
    // Insert helpers
    // -------------------------

    private UUID createUser(String prefix) {
        UUID id = UUID.randomUUID();
        createdUserIds.add(id);

        jdbcTemplate.update("""
                INSERT INTO users (id, email, nickname, password, created_at, deleted_at)
                VALUES (?, ?, ?, ?, ?, NULL)
                """,
                id,
                prefix + "-" + id + "@example.com",
                prefix + "-" + id,
                "password",
                Timestamp.from(Instant.parse("2026-01-12T00:00:00Z"))
        );
        return id;
    }

    private UUID createArticle() {
        UUID id = UUID.randomUUID();
        createdArticleIds.add(id);

        jdbcTemplate.update("""
                INSERT INTO articles (id, source, source_url, title, publish_date, summary, created_at, is_deleted)
                VALUES (?, ?, ?, ?, ?, ?, ?, FALSE)
                """,
                id,
                "NAVER",
                "https://example.com/article/" + id,
                "test article " + id,
                Timestamp.from(Instant.parse("2026-01-12T00:00:00Z")),
                "summary",
                Timestamp.from(Instant.parse("2026-01-12T00:00:00Z"))
        );
        return id;
    }

    private UUID createComment(UUID articleId, UUID userId, Instant createdAt, String content) {
        UUID id = UUID.randomUUID();
        createdCommentIds.add(id);

        jdbcTemplate.update("""
                INSERT INTO comments (id, user_id, article_id, content, created_at, is_deleted)
                VALUES (?, ?, ?, ?, ?, FALSE)
                """,
                id,
                userId,
                articleId,
                content,
                Timestamp.from(createdAt)
        );
        return id;
    }

    private void addLikes(UUID commentId, int count) {
        for (int i = 0; i < count; i++) {
            UUID likerId = createUser("liker");
            UUID likeId = UUID.randomUUID();
            createdLikeIds.add(likeId);

            jdbcTemplate.update("""
                    INSERT INTO comment_likes (id, user_id, comment_id, created_at)
                    VALUES (?, ?, ?, ?)
                    """,
                    likeId,
                    likerId,
                    commentId,
                    Timestamp.from(Instant.parse("2026-01-12T00:00:10Z"))
            );
        }
    }
}
