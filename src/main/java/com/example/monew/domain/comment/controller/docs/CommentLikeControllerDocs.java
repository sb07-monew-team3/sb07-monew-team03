package com.example.monew.domain.comment.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.UUID;

@Tag(name = "댓글 좋아요", description = "댓글 좋아요 API")
public interface CommentLikeControllerDocs {
// 댓글 좋아요 등록
    @Operation(
            summary = "댓글 좋아요",
            description = "댓글에 좋아요를 등록합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "처리 성공"),
            @ApiResponse(responseCode = "404", description = "댓글 또는 유저가 존재하지 않음")
    })
    ResponseEntity<Void> like(
            @Parameter(description = "요청자 ID", required = true)
            @RequestHeader("MoNew-Request-User-ID") UUID userId,

            @Parameter(description = "댓글 ID", required = true)
            @PathVariable UUID commentId
    );
// 댓글 좋아요 취소
    @Operation(
            summary = "댓글 좋아요 취소",
            description = "댓글 좋아요를 취소합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "처리 성공"),
            @ApiResponse(responseCode = "404", description = "댓글 또는 유저가 존재하지 않음")
    })
    ResponseEntity<Void> unlike(
            @Parameter(description = "요청자 ID", required = true)
            @RequestHeader("MoNew-Request-User-ID") UUID userId,

            @Parameter(description = "댓글 ID", required = true)
            @PathVariable UUID commentId
    );
}
