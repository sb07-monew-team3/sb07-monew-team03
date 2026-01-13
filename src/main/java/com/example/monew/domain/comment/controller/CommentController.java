package com.example.monew.domain.comment.controller;

import com.example.monew.domain.comment.dto.CommentCreateRequest;
import com.example.monew.domain.comment.dto.CommentCursorListRequest;
import com.example.monew.domain.comment.dto.CommentResponse;
import com.example.monew.domain.comment.dto.CommentUpdateRequest;
import com.example.monew.domain.comment.dto.CursorPageResponse;
import com.example.monew.domain.comment.service.CommentQueryService;
import com.example.monew.domain.comment.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
public class CommentController {

    private final CommentService commentService;
    private final CommentQueryService commentQueryService;

    @Operation(
            summary = "댓글 목록 조회",
            description = """
                    조건에 맞는 댓글 목록을 조회합니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터(orderBy/direction/page/limit)"),
            @ApiResponse(responseCode = "404", description = "기사 ID가 존재하지 않음")
    })
    @GetMapping
    public ResponseEntity<Page<CommentResponse>> list(
            @Parameter(description = "요청자 ID", required = true, example = "7b750a92-56bd-496e-8df6-061c0cb99a9c")
            @RequestHeader("Monew-Request-User-ID") UUID userId,

            @Parameter(description = "기사 ID", required = true, example = "ca233937-9648-4e27-91e0-bbd6d7189d65")
            @RequestParam UUID articleId,

            @Parameter(description = "정렬 기준 (createdAt | likeCount)", example = "createdAt")
            @RequestParam(required = false) String orderBy,

            @Parameter(description = "정렬 방향 (ASC | DESC)", example = "DESC")
            @RequestParam(required = false) Sort.Direction direction,

            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(required = false) Integer page,

            @Parameter(description = "페이지 크기 (기본 20, 최대 50)", example = "20")
            @RequestParam(required = false) Integer limit
    ) {
        return ResponseEntity.ok(
                commentService.list(userId, articleId, orderBy, direction, page, limit)
        );
    }

    @Operation(
            summary = "댓글 등록",
            description = "댓글을 등록합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "등록 성공"),
            @ApiResponse(responseCode = "400", description = "요청 바디 검증 실패"),
            @ApiResponse(responseCode = "404", description = "기사/유저가 존재하지 않음")
    })
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

    @Operation(
            summary = "댓글 수정",
            description = "댓글을 수정합니다. (본인만 가능)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "요청 바디 검증 실패"),
            @ApiResponse(responseCode = "403", description = "본인 댓글이 아님"),
            @ApiResponse(responseCode = "404", description = "댓글이 존재하지 않음")
    })
    @PatchMapping("/{commentId}")
    public ResponseEntity<CommentResponse> update(
            @Parameter(description = "요청자 ID", required = true, example = "7b750a92-56bd-496e-8df6-061c0cb99a9c")
            @RequestHeader("Monew-Request-User-ID") UUID userId,

            @Parameter(description = "댓글 ID", required = true, example = "75788fca-c628-471b-82ba-a7a8732c9fe6")
            @PathVariable UUID commentId,

            @Valid @RequestBody CommentUpdateRequest request
    ) {
        return ResponseEntity.ok(commentService.update(userId, commentId, request.getContent()));
    }

    @Operation(
            summary = "댓글 논리 삭제",
            description = """
                    댓글을 논리 삭제(soft delete)합니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "본인 댓글이 아님"),
            @ApiResponse(responseCode = "404", description = "댓글이 존재하지 않음")
    })
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> softDelete(
            @Parameter(hidden = true)
            @RequestHeader(value = "Monew-Request-User-ID", required = false) UUID userId,

            @Parameter(description = "댓글 ID", required = true, example = "75788fca-c628-471b-82ba-a7a8732c9fe6")
            @PathVariable UUID commentId
    ) {
        commentService.softDelete(userId, commentId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "댓글 물리 삭제",
            description = """
                    댓글을 물리 삭제(hard delete)합니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "본인 댓글이 아님"),
            @ApiResponse(responseCode = "404", description = "댓글이 존재하지 않음")
    })
    @DeleteMapping("/{commentId}/hard")
    public ResponseEntity<Void> hardDelete(
            @Parameter(hidden = true)
            @RequestHeader(value = "Monew-Request-User-ID", required = false) UUID userId,

            @Parameter(description = "댓글 ID", required = true, example = "75788fca-c628-471b-82ba-a7a8732c9fe6")
            @PathVariable UUID commentId
    ) {
        commentService.hardDelete(userId, commentId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "댓글 목록 조회 (커서 페이지네이션)",
            description = """
                    커서 기반 페이지네이션으로 댓글 목록을 조회합니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터(orderBy/direction/cursor/after/limit)"),
            @ApiResponse(responseCode = "404", description = "기사 ID가 존재하지 않음")
    })
    @GetMapping("/cursor")
    public CursorPageResponse<CommentResponse> listByCursor(
            @Parameter(description = "요청자 ID", required = true, example = "7b750a92-56bd-496e-8df6-061c0cb99a9c")
            @RequestHeader(value = "Monew-Request-User-ID", required = false) UUID userId,

            @Parameter(description = "기사 ID", required = true, example = "ca233937-9648-4e27-91e0-bbd6d7189d65")
            @RequestParam UUID articleId,

            @Parameter(description = "정렬 기준 (createdAt | likeCount, 기본 createdAt)", example = "createdAt")
            @RequestParam(required = false) String orderBy,

            @Parameter(description = "정렬 방향 (ASC | DESC, 기본 DESC)", example = "DESC")
            @RequestParam(required = false) Sort.Direction direction,

            @Parameter(description = "커서 값(Base64URL)", example = "MjAyNi0wMS0xM1QwMDowMDowMFp8NzU3ODhmY2EtYzYyOC00NzFiLTgyYmEtYTdhODczMmM5ZmU2")
            @RequestParam(required = false) String cursor,

            @Parameter(description = "다음 페이지 여부 (기본 true)", example = "true")
            @RequestParam(required = false) Boolean after,

            @Parameter(description = "조회 개수 (기본 20, 최대 50)", example = "20")
            @RequestParam(required = false) Integer limit
    ) {
        CommentCursorListRequest req = new CommentCursorListRequest(
                articleId, orderBy, direction, cursor, after, limit
        );
        return commentQueryService.getCommentsByCursor(userId, req);
    }
}
