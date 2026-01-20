package com.example.monew.domain.comment.integration;

import com.example.monew.domain.article.entity.Article;
import com.example.monew.domain.comment.dto.CommentCreateRequest;
import com.example.monew.domain.user.entity.User;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(
        properties = {
                "spring.main.allow-bean-definition-overriding=true"
        }
)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.main.allow-bean-definition-overriding=true"
})
@Transactional
class CommentIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    EntityManager em;

    @Test
    @DisplayName("댓글 생성 -> 목록 조회 -> softDelete -> 목록에서 제외")
    void comment_flow_create_list_softDelete() throws Exception {
        Instant base = Instant.parse("2026-01-01T00:00:00Z");

        User user = persistUser(base);
        Article article = persistArticle(base);

        em.flush();
        em.clear();

        UUID userId = user.getId();
        UUID articleId = article.getId();

        CommentCreateRequest createReq = new CommentCreateRequest(
                articleId,
                userId,
                "hello"
        );

        String createBody = mockMvc.perform(post("/api/comments")
                        .header("Monew-Request-User-ID", userId.toString())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq))
                        .accept(APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("hello"))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.articleId").value(articleId.toString()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode createdJson = objectMapper.readTree(createBody);
        String commentId = createdJson.get("id").asText();

        mockMvc.perform(get("/api/comments")
                        .header("Monew-Request-User-ID", userId.toString())
                        .param("articleId", articleId.toString())
                        .param("orderBy", "createdAt")
                        .param("direction", "DESC")
                        .param("limit", "10")
                        .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(commentId));

        mockMvc.perform(delete("/api/comments/{commentId}", commentId)
                        .header("Monew-Request-User-ID", userId.toString()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/comments")
                        .header("Monew-Request-User-ID", userId.toString())
                        .param("articleId", articleId.toString())
                        .param("orderBy", "createdAt")
                        .param("direction", "DESC")
                        .param("limit", "10")
                        .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));
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
}
