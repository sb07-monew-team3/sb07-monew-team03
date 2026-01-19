package com.example.monew.domain.interest.docs;

import com.example.monew.domain.interest.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@Tag(name = "관심사 관리", description = "관심사 관련 API")
public interface InterestControllerDocs {

    @Operation(summary = "관심사 등록", description = "새로운 관심사를 등록합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "등록 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = InterestDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "유사 관심사 중복",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = InterestDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (입력값 검증 실패)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = InterestDto.class)
                    )
            ),
    })
    ResponseEntity<InterestDto> registerInterest(@RequestBody InterestRegisterRequest request);


    @Operation(summary = "관심사 정보 수정", description = "관심사의 키워드를 수정합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "수정 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = InterestDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (입력값 검증 실패)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = InterestDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "관심사 정보 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = InterestDto.class)
                    )
            )

    })
    ResponseEntity<InterestDto> update(
            @Parameter(description = "관심사 ID") @PathVariable UUID interestId,
            @RequestBody InterestUpdateRequest request);


    @Operation(summary = "관심사 물리 삭제", description = "관심사를 물리적으로 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "관심사 정보 없음")
    })
    ResponseEntity<Void> hardDelete(@Parameter(description = "관심사 ID") @PathVariable UUID interestId);

    @Operation(summary = "관심사 목록 조회", description = "조건에 맞는 관심사 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CursorPageResponseInterestDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (정렬 기준 오류, 페이지네이션 파라미터 오류 등)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CursorPageResponseInterestDto.class)
                    )
            ),
    })
    ResponseEntity<CursorPageResponseInterestDto> search(
            @Parameter(description = "검색어(관심사 이름, 키워드)", required = false, example = "동물")
            @RequestParam(required = false) String keyword,

            @Parameter(description = "정렬 속성 이름", required = true,
                    schema = @Schema(allowableValues = {"name", "subscriberCount"}))
            @RequestParam(required = true) String orderBy,

            @Parameter(description = "정렬 방향 (ASC, DESC)", required = true,
                    schema = @Schema(allowableValues = {"ASC", "DESC"}))
            @RequestParam(required = true) String direction,

            @Parameter(description = "커서 값", required = false)
            @RequestParam(required = false) String cursor,

            @Parameter(description = "보조 커서(createdAt) 값", required = false,
                    schema = @Schema(type = "string", format = "data-time"))
            @RequestParam(required = false) Instant after,

            @Parameter(description = "커서 페이지 크기", required = true, example = "50")
            @RequestParam(required = true) int limit,

            @Parameter(description = "요청자 ID", schema = @Schema(type = "string", format = "uuid"))
            @RequestHeader("Monew-Request-User-ID") UUID userId);


    @Operation(summary = "관심사 구독", description = "관심사를 구독합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "구독 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SubscriptionDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "관심사 정보 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SubscriptionDto.class)
                    )
            ),
    })
    ResponseEntity<SubscriptionDto> subscribe(
            @Parameter(description = "관심사 ID")
            @PathVariable UUID interestId,

            @Parameter(description = "요청자 ID", schema = @Schema(type = "string", format = "uuid"))
            @RequestHeader("Monew-Request-User-ID") UUID userId);


    @Operation(summary = "관심사 구독 취소", description = "관심사를 구독을 취소합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "구독 취소 성공"),
            @ApiResponse(responseCode = "404", description = "관심사 정보 없음")
    })
    ResponseEntity<Void> unsubscribe(
            @Parameter(description = "관심사 ID")
            @PathVariable UUID interestId,

            @Parameter(description = "요청자 ID", schema = @Schema(type = "string", format = "uuid"))
            @RequestHeader("Monew-Request-User-ID") UUID userId);
}
