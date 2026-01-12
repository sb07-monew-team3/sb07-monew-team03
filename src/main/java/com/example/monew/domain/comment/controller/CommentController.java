package com.example.monew.domain.comment.controller;

import com.example.monew.domain.comment.dto.*;
import com.example.monew.domain.comment.service.CommentQueryService;
import com.example.monew.domain.comment.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;

    private final CommentQueryService commentQueryService;

    @GetMapping
    public ResponseEntity<Page<CommentResponse>> list(
            @RequestHeader("Monew-Request-User-ID") UUID userId,
            @RequestParam UUID articleId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(commentService.list(userId, articleId, pageable));
    }

    @PostMapping
    public ResponseEntity<CommentResponse> create(
            @Valid @RequestBody CommentCreateRequest request
    ) {
        CommentResponse response = commentService.create(request.getUserId(), request.getArticleId(), request.getContent());
        return ResponseEntity.created(URI.create("/api/comments/" + response.getId())).body(response);
    }

    @PatchMapping("/{commentId}")
    public ResponseEntity<CommentResponse> update(
            @RequestHeader("Monew-Request-User-ID") UUID userId,
            @PathVariable UUID commentId,
            @Valid @RequestBody CommentUpdateRequest request
    ) {
        return ResponseEntity.ok(commentService.update(userId, commentId, request.getContent()));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> softDelete(
            @RequestHeader("Monew-Request-User-ID") UUID userId,
            @PathVariable UUID commentId
    ) {
        commentService.softDelete(userId, commentId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{commentId}/hard")
    public ResponseEntity<Void> hardDelete(
            @RequestHeader("Monew-Request-User-ID") UUID userId,
            @PathVariable UUID commentId
    ) {
        commentService.hardDelete(userId, commentId);
        return ResponseEntity.noContent().build();
    }
    @Operation(summary = "댓글 목록 조회 (커서 페이지네이션)")
    @GetMapping("/cursor")
    public CursorPageResponse<CommentResponse> listByCursor(
            @RequestParam UUID articleId,
            @Parameter(description = "정렬 기준 (현재 createdAt만 지원, 기본 createdAt)")
            @RequestParam(required = false) String orderBy,
            @Parameter(description = "정렬 방향 (ASC|DESC, 기본 DESC)")
            @RequestParam(required = false) Sort.Direction direction,
            @Parameter(description = "커서 (Base64URL(createdAt|id))")
            @RequestParam(required = false) String cursor,
            @Parameter(description = "다음 페이지 여부 (기본 true)")
            @RequestParam(required = false) Boolean after,
            @Parameter(description = "조회 개수 (기본 20, 최대 50)")
            @RequestParam(required = false) Integer limit
    ) {
        CommentCursorListRequest req = new CommentCursorListRequest(
                articleId, orderBy, direction, cursor, after, limit
        );
        return commentQueryService.getCommentsByCursor(req);
    }

}
