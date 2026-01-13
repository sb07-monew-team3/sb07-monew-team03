package com.example.monew.domain.comment.controller.docs;

import com.example.monew.domain.comment.dto.CommentCreateRequest;
import com.example.monew.domain.comment.dto.CommentResponse;
import com.example.monew.domain.comment.dto.CommentUpdateRequest;
import com.example.monew.domain.comment.dto.CursorPageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;


@Tag(name = "댓글", description = "댓글 API")
public interface CommentControllerDocs {
//댓글 목록 조회 (페이지 네이션)
    @Operation(
            summary = "댓글 목록 조회 (페이지네이션)",
            description = "기사 ID로 댓글 목록을 페이지네이션 방식으로 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CommentResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "content": [
                                        {
                                          "id": "75788fca-c628-471b-82ba-a7a8732c9fe6",
                                          "articleId": "ca233937-9648-4e27-91e0-bbd6d7189d65",
                                          "userId": "7b750a92-56bd-496e-8df6-061c0cb99a9c",
                                          "userNickname": "태훈",
                                          "content": "댓글 내용",
                                          "likeCount": 3,
                                          "likedByMe": true,
                                          "createdAt": "2026-01-13T00:00:00Z"
                                        }
                                      ]
                                    }
                                    """)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터(orderBy/direction/page/limit)"),
            @ApiResponse(responseCode = "404", description = "기사 ID가 존재하지 않음")
    })
    ResponseEntity<Page<CommentResponse>> list(
            @Parameter(description = "요청자 ID", required = true)
            @RequestHeader("Monew-Request-User-ID") UUID userId,

            @Parameter(description = "기사 ID", required = true)
            @RequestParam UUID articleId,

            @Parameter(description = "정렬 기준 (createdAt | likeCount)")
            @RequestParam(required = false) String orderBy,

            @Parameter(description = "정렬 방향 (ASC | DESC)", example = "DESC")
            @RequestParam(required = false) Sort.Direction direction,

            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(required = false) Integer page,

            @Parameter(description = "페이지 크기 (기본 20, 최대 50)", example = "20")
            @RequestParam(required = false) Integer limit
    );
// 댓글 등록
    @Operation(
            summary = "댓글 등록",
            description = "새로운 댓글을 등록합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "등록 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CommentResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "id": "75788fca-c628-471b-82ba-a7a8732c9fe6",
                                      "articleId": "ca233937-9648-4e27-91e0-bbd6d7189d65",
                                      "userId": "7b750a92-56bd-496e-8df6-061c0cb99a9c",
                                      "userNickname": "태훈",
                                      "content": "댓글 내용",
                                      "likeCount": 0,
                                      "likedByMe": false,
                                      "createdAt": "2026-01-13T00:00:00Z"
                                    }
                                    """)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "요청 바디 검증 실패"),
            @ApiResponse(responseCode = "404", description = "기사/유저가 존재하지 않음")
    })
    ResponseEntity<CommentResponse> create(
            @RequestBody CommentCreateRequest request
    );
// 댓글 수정
    @Operation(
            summary = "댓글 내용 수정",
            description = "댓글을 수정합니다. (본인만 가능)"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "수정 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CommentResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "요청 바디 검증 실패"),
            @ApiResponse(responseCode = "403", description = "본인 댓글이 아님"),
            @ApiResponse(responseCode = "404", description = "댓글이 존재하지 않음")
    })
    ResponseEntity<CommentResponse> update(
            @Parameter(description = "요청자 ID", required = true, example = "commentid")
            @RequestHeader("Monew-Request-User-ID") UUID userId,

            @Parameter(description = "댓글 ID", required = true, example="Monew-Request-User-Id")
            @PathVariable UUID commentId,

            @RequestBody CommentUpdateRequest request
    );
// 댓글 논리 삭제
    @Operation(
            summary = "댓글 논리 삭제",
            description = "댓글을 논리적으로 삭제합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "본인 댓글이 아님"),
            @ApiResponse(responseCode = "404", description = "댓글이 존재하지 않음")
    })
    ResponseEntity<Void> softDelete(
            @Parameter(hidden = true)
            @RequestHeader("Monew-Request-User-ID") UUID userId,
            @Parameter(description = "댓글 ID", required = true, example = "commentId")
            @PathVariable UUID commentId
    );
// 댓글 물리 삭제
    @Operation(
            summary = "댓글 물리 삭제",
            description = "댓글을 물리 삭제(hard delete)합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "본인 댓글이 아님"),
            @ApiResponse(responseCode = "404", description = "댓글이 존재하지 않음")
    })
    ResponseEntity<Void> hardDelete(
            @Parameter(hidden = true)
            @RequestHeader("Monew-Request-User-ID") UUID userId,

            @Parameter(description = "댓글 ID", required = true)
            @PathVariable UUID commentId
    );
// 댓글 목록 조회 (커서페이지네이션)
    @Operation(
            summary = "댓글 목록 조회 (커서 페이지네이션)",
            description = "커서 기반 페이지네이션으로 댓글 목록을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CursorPageResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "items": [
                                        {
                                          "id": "75788fca-c628-471b-82ba-a7a8732c9fe6",
                                          "articleId": "ca233937-9648-4e27-91e0-bbd6d7189d65",
                                          "userId": "7b750a92-56bd-496e-8df6-061c0cb99a9c",
                                          "userNickname": "taehun",
                                          "content": "좋아요!",
                                          "likeCount": 1,
                                          "likedByMe": false,
                                          "createdAt": "2026-01-13T00:00:00Z"
                                        }
                                      ],
                                      "nextCursor": "string",
                                        "nextAfter": "2025-04-06T15:04:05.000Z",
                                        "size": 10,
                                        "totalElements": 100,
                                        "hasNext": true
                                    }
                                    """)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터(orderBy/direction/cursor/after/limit)"),
            @ApiResponse(responseCode = "404", description = "기사 ID가 존재하지 않음")
    })
    CursorPageResponse<CommentResponse> listByCursor(
            @Parameter(description = "요청자 ID", required = true)
            @RequestHeader("Monew-Request-User-ID") UUID userId,

            @Parameter(description = "기사 ID", required = true, example = "articleId")
            @RequestParam UUID articleId,

            @Parameter(description = "정렬 속성 이름", example = "createdAt")
            @RequestParam(required = false) String orderBy,

            @Parameter(description = "정렬 방향 (ASC | DESC, 기본 DESC)", example = "DESC")
            @RequestParam(required = false) Sort.Direction direction,

            @Parameter(description = "커서 값(Base64URL)")
            @RequestParam(required = false) String cursor,

            @Parameter(description = "다음 페이지 여부 (기본 true)", example = "true")
            @RequestParam(required = false) Boolean after,

            @Parameter(description = "조회 개수 (기본 20, 최대 50)", example = "20")
            @RequestParam(required = false) Integer limit
    );
}
