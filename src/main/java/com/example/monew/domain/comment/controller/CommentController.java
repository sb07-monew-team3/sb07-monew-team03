package com.example.monew.domain.comment.controller;

import com.example.monew.domain.comment.dto.CommentCreateRequest;
import com.example.monew.domain.comment.dto.CommentResponse;
import com.example.monew.domain.comment.dto.CommentUpdateRequest;
import com.example.monew.domain.comment.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;

    @GetMapping
    public ResponseEntity<Page<CommentResponse>> list(
            @RequestHeader("MoNew-Request-User-ID") UUID userId,
            @RequestParam UUID articleId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(commentService.list(userId, articleId, pageable));
    }

    @PostMapping
    public ResponseEntity<CommentResponse> create(
            @RequestHeader("MoNew-Request-User-ID") UUID userId,
            @Valid @RequestBody CommentCreateRequest request
    ) {
        CommentResponse response = commentService.create(userId, request.getArticleId(), request.getContent());
        return ResponseEntity.created(URI.create("/api/comments/" + response.getId())).body(response);
    }

    @PatchMapping("/{commentId}")
    public ResponseEntity<CommentResponse> update(
            @RequestHeader("MoNew-Request-User-ID") UUID userId,
            @PathVariable UUID commentId,
            @Valid @RequestBody CommentUpdateRequest request
    ) {
        return ResponseEntity.ok(commentService.update(userId, commentId, request.getContent()));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> softDelete(
            @RequestHeader("MoNew-Request-User-ID") UUID userId,
            @PathVariable UUID commentId
    ) {
        commentService.softDelete(userId, commentId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{commentId}/hard")
    public ResponseEntity<Void> hardDelete(
            @RequestHeader("MoNew-Request-User-ID") UUID userId,
            @PathVariable UUID commentId
    ) {
        commentService.hardDelete(userId, commentId);
        return ResponseEntity.noContent().build();
    }


}
