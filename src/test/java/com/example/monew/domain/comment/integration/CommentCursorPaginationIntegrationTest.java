package com.example.monew.domain.comment.integration;

import com.example.monew.domain.comment.support.CommentCursor;
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
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "CLIENT_ID=test-client-id",
        "CLIENT_SECRET=test-client-secret"
})
@AutoConfigureMockMvc
class CommentCursorPaginationIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired JdbcTemplate jdbcTemplate;

    private final Set<UUID> createdUserIds = new HashSet<>();
    private final Set<UUID> createdArticleIds = new HashSet<>();
    private final Set<UUID> createdCommentIds = new HashSet<>();

    @AfterEach
    void tearDown() {
        for (UUID commentId : createdCommentIds) {
            jdbcTemplate.update("DELETE FROM comment_likes WHERE comment_id = ?", commentId);
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
    @DisplayName("커서 첫 페이지: limit=2 DESC면 2개 반환 + hasNext=true + nextCursor 존재")
    void cursor_firstPage_limit2_desc() throws Exception {
        UUID userId = createUser();
        UUID articleId = createArticle();

        UUID c1 = createComment(articleId, userId, Instant.parse("2026-01-12T00:00:03Z"), "c1");
        UUID c2 = createComment(articleId, userId, Instant.parse("2026-01-12T00:00:02Z"), "c2");
        UUID c3 = createComment(articleId, userId, Instant.parse("2026-01-12T00:00:01Z"), "c3");

        String json = mockMvc.perform(
                        get("/api/comments/cursor")
                                .param("articleId", articleId.toString())
                                .param("limit", "2")
                                .param("direction", "DESC")
                )
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode root = objectMapper.readTree(json);

        assertThat(root.get("items").size()).isEqualTo(2);
        assertThat(root.get("hasNext").asBoolean()).isTrue();
        assertThat(root.get("nextCursor").asText()).isNotBlank();

        UUID returned1 = UUID.fromString(root.get("items").get(0).get("id").asText());
        UUID returned2 = UUID.fromString(root.get("items").get(1).get("id").asText());
        assertThat(returned1).isEqualTo(c1);
        assertThat(returned2).isEqualTo(c2);
    }

    @Test
    @DisplayName("커서 다음 페이지: nextCursor로 재호출하면 중복 없이 이어지고 hasNext=false가 된다")
    void cursor_nextPage_noDuplicate() throws Exception {
        UUID userId = createUser();
        UUID articleId = createArticle();

        UUID c1 = createComment(articleId, userId, Instant.parse("2026-01-12T00:00:03Z"), "c1");
        UUID c2 = createComment(articleId, userId, Instant.parse("2026-01-12T00:00:02Z"), "c2");
        UUID c3 = createComment(articleId, userId, Instant.parse("2026-01-12T00:00:01Z"), "c3");

        String json1 = mockMvc.perform(
                        get("/api/comments/cursor")
                                .param("articleId", articleId.toString())
                                .param("limit", "2")
                                .param("direction", "DESC")
                )
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode root1 = objectMapper.readTree(json1);
        String nextCursor = root1.get("nextCursor").asText();

        UUID p1_1 = UUID.fromString(root1.get("items").get(0).get("id").asText());
        UUID p1_2 = UUID.fromString(root1.get("items").get(1).get("id").asText());

        String json2 = mockMvc.perform(
                        get("/api/comments/cursor")
                                .param("articleId", articleId.toString())
                                .param("limit", "2")
                                .param("direction", "DESC")
                                .param("cursor", nextCursor)
                )
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode root2 = objectMapper.readTree(json2);

        assertThat(root2.get("items").size()).isEqualTo(1);
        assertThat(root2.get("hasNext").asBoolean()).isFalse();
        assertThat(root2.get("nextCursor").isNull() || root2.get("nextCursor").asText().isBlank()).isTrue();

        UUID p2_1 = UUID.fromString(root2.get("items").get(0).get("id").asText());

        // 중복 없어야 함
        assertThat(p2_1).isEqualTo(c3);
        assertThat(p2_1).isNotIn(p1_1, p1_2);
    }

    @Test
    @DisplayName("동점(createdAt 동일) 케이스: createdAt+id 복합 커서로 중복/누락 없이 페이지가 이어진다")
    void cursor_tie_breaker_createdAt_same() throws Exception {
        UUID userId = createUser();
        UUID articleId = createArticle();

        Instant same = Instant.parse("2026-01-12T00:00:02Z");

        UUID newest = createComment(articleId, userId, Instant.parse("2026-01-12T00:00:03Z"), "newest");
        UUID tie1   = createComment(articleId, userId, same, "tie1");
        UUID tie2   = createComment(articleId, userId, same, "tie2");
        UUID oldest = createComment(articleId, userId, Instant.parse("2026-01-12T00:00:01Z"), "oldest");

        String json1 = mockMvc.perform(
                        get("/api/comments/cursor")
                                .param("articleId", articleId.toString())
                                .param("limit", "2")
                                .param("direction", "DESC")
                )
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode root1 = objectMapper.readTree(json1);
        String nextCursor = root1.get("nextCursor").asText();

        UUID p1_1 = UUID.fromString(root1.get("items").get(0).get("id").asText());
        UUID p1_2 = UUID.fromString(root1.get("items").get(1).get("id").asText());

        String json2 = mockMvc.perform(
                        get("/api/comments/cursor")
                                .param("articleId", articleId.toString())
                                .param("limit", "2")
                                .param("direction", "DESC")
                                .param("cursor", nextCursor)
                )
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode root2 = objectMapper.readTree(json2);

        assertThat(root2.get("items").size()).isEqualTo(2);
        UUID p2_1 = UUID.fromString(root2.get("items").get(0).get("id").asText());
        UUID p2_2 = UUID.fromString(root2.get("items").get(1).get("id").asText());

        Set<UUID> all = Set.of(p1_1, p1_2, p2_1, p2_2);
        assertThat(all).hasSize(4);

        assertThat(all).contains(newest, oldest);

        assertThat(all).contains(tie1, tie2);
    }
    private UUID createUser() {
        UUID id = UUID.randomUUID();
        createdUserIds.add(id);

        jdbcTemplate.update("""
                INSERT INTO users (id, email, nickname, password, created_at, deleted_at)
                VALUES (?, ?, ?, ?, ?, NULL)
                """,
                id,
                "test-" + id + "@example.com",
                "tester-" + id,
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
                INSERT INTO comments (id, article_id, user_id, content, created_at, is_deleted)
                VALUES (?, ?, ?, ?, ?, FALSE)
                """,
                id,
                articleId,
                userId,
                content,
                Timestamp.from(createdAt)
        );
        return id;
    }
}
