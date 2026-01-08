package com.example.monew.domain.comment.controller;

import com.example.monew.domain.comment.service.CommentLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comments")
public class CommentLikeController {

    private final CommentLikeService commentLikeService;

    @PostMapping("/{commentId}/comment-likes")
    public ResponseEntity<Void> like(
            @RequestHeader("MoNew-Request-User-ID") UUID userId,
            @PathVariable UUID commentId
    ) {
        commentLikeService.like(userId, commentId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{commentId}/comment-likes")
    public ResponseEntity<Void> unlike(
            @RequestHeader("MoNew-Request-User-ID") UUID userId,
            @PathVariable UUID commentId
    ) {
        commentLikeService.unlike(userId, commentId);
        return ResponseEntity.noContent().build();
    }
}
