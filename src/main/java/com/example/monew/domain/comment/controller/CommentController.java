package com.example.monew.domain.comment.controller;

import com.example.monew.domain.comment.controller.docs.CommentControllerDocs;
import com.example.monew.domain.comment.dto.CommentCreateRequest;
import com.example.monew.domain.comment.dto.CommentCursorListRequest;
import com.example.monew.domain.comment.dto.CommentResponse;
import com.example.monew.domain.comment.dto.CommentUpdateRequest;
import com.example.monew.domain.comment.dto.CursorPageResponse;
import com.example.monew.domain.comment.service.CommentQueryService;
import com.example.monew.domain.comment.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comments")
public class CommentController implements CommentControllerDocs {

    private final CommentService commentService;
    private final CommentQueryService commentQueryService;

    @GetMapping
    public ResponseEntity<Page<CommentResponse>> list(
            @RequestHeader("Monew-Request-User-ID") UUID userId,
            @RequestParam UUID articleId,
            @RequestParam(required = false) String orderBy,
            @RequestParam(required = false) Sort.Direction direction,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer limit
    ) {
        return ResponseEntity.ok(
                commentService.list(userId, articleId, orderBy, direction, page, limit)
        );
    }

    @PostMapping
    public ResponseEntity<CommentResponse> create(
            @Valid @RequestBody CommentCreateRequest request
    ) {
        CommentResponse response = commentService.create(
                request.getUserId(),
                request.getArticleId(),
                request.getContent()
        );
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
            @RequestHeader(value = "Monew-Request-User-ID", required = false) UUID userId,
            @PathVariable UUID commentId
    ) {
        commentService.softDelete(userId, commentId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{commentId}/hard")
    public ResponseEntity<Void> hardDelete(
            @RequestHeader(value = "Monew-Request-User-ID", required = false) UUID userId,
            @PathVariable UUID commentId
    ) {
        commentService.hardDelete(userId, commentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/cursor")
    public CursorPageResponse<CommentResponse> listByCursor(
            @RequestHeader(value = "Monew-Request-User-ID", required = false) UUID userId,
            @RequestParam UUID articleId,
            @RequestParam(required = false) String orderBy,
            @RequestParam(required = false) Sort.Direction direction,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Boolean after,
            @RequestParam(required = false) Integer limit
    ) {
        CommentCursorListRequest req = new CommentCursorListRequest(
                articleId, orderBy, direction, cursor, after, limit
        );
        return commentQueryService.getCommentsByCursor(userId, req);
    }
}
