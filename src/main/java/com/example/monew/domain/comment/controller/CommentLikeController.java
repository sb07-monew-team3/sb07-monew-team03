package com.example.monew.domain.comment.controller;

import com.example.monew.domain.comment.service.CommentLikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comments")
public class CommentLikeController {

    private final CommentLikeService commentLikeService;

    @Operation(
            summary = "댓글 좋아요",
            description = "댓글에 좋아요를 누릅니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "처리 성공"),
            @ApiResponse(responseCode = "404", description = "댓글 또는 유저가 존재하지 않음")
    })
    @PostMapping("/{commentId}/comment-likes")
    public ResponseEntity<Void> like(
            @Parameter(description = "요청자 ID", required = true, example = "7b750a92-56bd-496e-8df6-061c0cb99a9c")
            @RequestHeader("MoNew-Request-User-ID") UUID userId,

            @Parameter(description = "댓글 ID", required = true, example = "75788fca-c628-471b-82ba-a7a8732c9fe6")
            @PathVariable UUID commentId
    ) {
        commentLikeService.like(userId, commentId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "댓글 좋아요 취소",
            description = "댓글 좋아요를 취소합니다. "
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "처리 성공"),
            @ApiResponse(responseCode = "404", description = "댓글 또는 유저가 존재하지 않음")
    })
    @DeleteMapping("/{commentId}/comment-likes")
    public ResponseEntity<Void> unlike(
            @Parameter(description = "요청자 ID", required = true, example = "7b750a92-56bd-496e-8df6-061c0cb99a9c")
            @RequestHeader("MoNew-Request-User-ID") UUID userId,

            @Parameter(description = "댓글 ID", required = true, example = "75788fca-c628-471b-82ba-a7a8732c9fe6")
            @PathVariable UUID commentId
    ) {
        commentLikeService.unlike(userId, commentId);
        return ResponseEntity.noContent().build();
    }
}
