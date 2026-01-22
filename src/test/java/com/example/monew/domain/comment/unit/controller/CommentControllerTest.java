package com.example.monew.domain.comment.unit.controller;

import com.example.monew.domain.comment.controller.CommentController;
import com.example.monew.domain.comment.dto.CommentCreateRequest;
import com.example.monew.domain.comment.dto.CommentCursorPageResponse;
import com.example.monew.domain.comment.dto.CommentResponse;
import com.example.monew.domain.comment.dto.CommentUpdateRequest;
import com.example.monew.domain.comment.dto.CursorPageResponse;
import com.example.monew.domain.comment.service.CommentQueryService;
import com.example.monew.domain.comment.service.CommentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommentController.class)
class CommentControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    CommentService commentService;

    @MockBean
    CommentQueryService commentQueryService;

    @Test
    @DisplayName("list ok")
    void list_ok() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID articleId = UUID.randomUUID();

        CommentResponse r1 = new CommentResponse(
                UUID.randomUUID(),
                articleId,
                UUID.randomUUID(),
                "u1",
                "c1",
                Instant.parse("2026-01-16T00:00:00Z"),
                3L,
                true
        );
        CommentResponse r2 = new CommentResponse(
                UUID.randomUUID(),
                articleId,
                UUID.randomUUID(),
                "u2",
                "c2",
                Instant.parse("2026-01-15T00:00:00Z"),
                0L,
                false
        );

        CommentCursorPageResponse<CommentResponse> resp = CommentCursorPageResponse.of(
                List.of(r1, r2),
                "cur1",
                "true",
                2,
                2L,
                false
        );

        when(commentQueryService.getCommentsForFront(
                ArgumentMatchers.eq(userId),
                ArgumentMatchers.eq(articleId),
                ArgumentMatchers.eq("createdAt"),
                ArgumentMatchers.eq(Sort.Direction.DESC),
                ArgumentMatchers.isNull(),
                ArgumentMatchers.isNull(),
                ArgumentMatchers.eq(5)
        )).thenReturn(resp);

        mockMvc.perform(get("/api/comments")
                        .header("Monew-Request-User-ID", userId.toString())
                        .param("articleId", articleId.toString())
                        .param("orderBy", "createdAt")
                        .param("direction", "DESC")
                        .param("limit", "5")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(r1.getId().toString()))
                .andExpect(jsonPath("$.content[0].articleId").value(articleId.toString()))
                .andExpect(jsonPath("$.content[0].likeCount").value(3))
                .andExpect(jsonPath("$.content[0].likedByMe").value(true))
                .andExpect(jsonPath("$.nextCursor").value("cur1"))
                .andExpect(jsonPath("$.nextAfter").value("true"))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    @DisplayName("list header fail")
    void list_header_fail() throws Exception {
        UUID articleId = UUID.randomUUID();

        mockMvc.perform(get("/api/comments")
                        .param("articleId", articleId.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("listByCursor ok")
    void listByCursor_ok() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID articleId = UUID.randomUUID();

        CommentResponse r1 = new CommentResponse(
                UUID.randomUUID(),
                articleId,
                UUID.randomUUID(),
                "u1",
                "c1",
                Instant.parse("2026-01-16T00:00:00Z"),
                2L,
                false
        );

        CursorPageResponse<CommentResponse> resp = CursorPageResponse.of(
                List.of(r1),
                "cur2",
                false
        );

        when(commentQueryService.getCommentsByCursor(
                ArgumentMatchers.eq(userId),
                ArgumentMatchers.any()
        )).thenReturn(resp);

        mockMvc.perform(get("/api/comments/cursor")
                        .header("Monew-Request-User-ID", userId.toString())
                        .param("articleId", articleId.toString())
                        .param("orderBy", "likeCount")
                        .param("direction", "DESC")
                        .param("cursor", "cur1")
                        .param("after", "true")
                        .param("limit", "5")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].id").value(r1.getId().toString()))
                .andExpect(jsonPath("$.nextCursor").value("cur2"))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    @DisplayName("create ok")
    void create_ok() throws Exception {
        UUID headerUserId = UUID.randomUUID();
        UUID bodyUserId = UUID.randomUUID();
        UUID articleId = UUID.randomUUID();

        CommentCreateRequest req = new CommentCreateRequest(bodyUserId, articleId, "hello");

        UUID createdId = UUID.randomUUID();
        CommentResponse resp = new CommentResponse(
                createdId,
                articleId,
                bodyUserId,
                "me",
                "hello",
                Instant.parse("2026-01-16T00:00:00Z"),
                0L,
                false
        );

        when(commentService.create(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
                .thenReturn(resp);

        mockMvc.perform(post("/api/comments")
                        .header("Monew-Request-User-ID", headerUserId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/comments/" + createdId))
                .andExpect(jsonPath("$.id").value(createdId.toString()))
                .andExpect(jsonPath("$.content").value("hello"));

        verify(commentService).create(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("update ok")
    void update_ok() throws Exception {
        UUID headerUserId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();
        UUID articleId = UUID.randomUUID();

        CommentUpdateRequest req = new CommentUpdateRequest("updated");

        CommentResponse resp = new CommentResponse(
                commentId,
                articleId,
                headerUserId,
                "me",
                "updated",
                Instant.parse("2026-01-16T00:00:00Z"),
                0L,
                false
        );

        when(commentService.update(
                ArgumentMatchers.eq(headerUserId),
                ArgumentMatchers.eq(commentId),
                ArgumentMatchers.eq("updated")
        )).thenReturn(resp);

        mockMvc.perform(patch("/api/comments/{commentId}", commentId)
                        .header("Monew-Request-User-ID", headerUserId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(commentId.toString()))
                .andExpect(jsonPath("$.content").value("updated"));

        verify(commentService).update(headerUserId, commentId, "updated");
    }

    @Test
    @DisplayName("softDelete ok")
    void softDelete_ok() throws Exception {
        UUID headerUserId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();

        doNothing().when(commentService).softDelete(headerUserId, commentId);

        mockMvc.perform(delete("/api/comments/{commentId}", commentId)
                        .header("Monew-Request-User-ID", headerUserId.toString()))
                .andExpect(status().isNoContent());

        verify(commentService).softDelete(headerUserId, commentId);
    }

    @Test
    @DisplayName("hardDelete ok")
    void hardDelete_ok() throws Exception {
        UUID headerUserId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();

        doNothing().when(commentService).hardDelete(headerUserId, commentId);

        mockMvc.perform(delete("/api/comments/{commentId}/hard", commentId)
                        .header("Monew-Request-User-ID", headerUserId.toString()))
                .andExpect(status().isNoContent());

        verify(commentService).hardDelete(headerUserId, commentId);
    }
}
