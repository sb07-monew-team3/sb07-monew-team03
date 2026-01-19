package com.example.monew.domain.article.docs;

import com.example.monew.domain.article.dto.*;
import com.example.monew.global.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Tag(name="기사 관리", description = "기사 관련 API")
public interface ArticleControllerDocs {

    @Operation(summary = "기사 목록 조회", description = "조건에 맞는 기사 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CursorPageResponseArticleDto.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "content": [
                                                    {
                                                        "id": "4303be14-96f8-4319-8868-fc1c586ce33c",
                                                        "source": "NAVER",
                                                        "sourceUrl": "http://www.beyondpost.co.kr/view.php?ud=2026011613414086946cf2d78c68_30",
                                                        "title": "제로마켓, 2025 KBS N 브랜드어워즈 ‘해외선물 서비스’ 부문 대상 수상",
                                                        "publishDate": "2026-01-16T13:44:00",
                                                        "summary": "또한 제로마켓은 2024–25 시즌 잉글리시 프리미어리그(EPL) 소속 구단 울버햄튼 원더러스(Wolverhampton Wanderers FC)의 공식 스폰서로 활동했으며, 대한민국 축구 국가대표 황희찬 선수가 활약중인 구단과의 협업을 통해... ",
                                                        "commentCount": 0,
                                                        "viewCount": 0,
                                                        "viewedByMe": false
                                                    },
                                                    {
                                                        "id": "087f0f02-d093-4afc-b83d-18b18e4a176d",
                                                        "source": "NAVER",
                                                        "sourceUrl": "https://www.interfootball.co.kr/news/articleView.html?idxno=677829",
                                                        "title": "허정무, 이영표, 박지성 다음 황희찬...'네덜란드 명문' PSV가 노린다! &quot;...",
                                                        "publishDate": "2026-01-16T13:45:00",
                                                        "summary": "황희찬이 네덜란드 명문 PSV 아인트호벤의 관심을 받고 있다는 소식이다. 네덜란드 '사커 뉴스'는 15일(한국시간) &quot;PSV가 황희찬을 영입 대상으로 고려하고 있다. 공격진 보강을 위해 울버햄튼 원더러스 소속 황희찬에게... ",
                                                        "commentCount": 0,
                                                        "viewCount": 0,
                                                        "viewedByMe": false
                                                    },
                                                    {
                                                        "id": "281363b8-efc6-4e5e-9489-0864395db0c5",
                                                        "source": "NAVER",
                                                        "sourceUrl": "https://www.bntnews.co.kr/article/view/bnt202601160167",
                                                        "title": "맨체스터 더비→아시안컵, 쿠플에서 선보이는 빅 매치",
                                                        "publishDate": "2026-01-16T14:22:00",
                                                        "summary": "Guy’ 황희찬이 리그 2위 맨체스터 시티 사냥에 나선다. 오는 24일 울버햄튼은 맨시티와 리그 23라운드... 그 가운데 황희찬이 최근 3경기 1골 2도움으로 경기력을 끌어올리며 팀을 부진의 늪에서 끌어올리는 데 앞장서고... ",
                                                        "commentCount": 0,
                                                        "viewCount": 0,
                                                        "viewedByMe": false
                                                    },
                                                    {
                                                        "id": "0c389aa4-edec-427a-9cc7-bdb5c3ebc832",
                                                        "source": "NAVER",
                                                        "sourceUrl": "https://www.stnsports.co.kr/news/articleView.html?idxno=310758",
                                                        "title": "韓 초대박! 허정무·이영표·박지성 그리고 황희찬? PSV 이적설 등장 &quot;이...",
                                                        "publishDate": "2026-01-16T14:59:00",
                                                        "summary": "강의택 기자┃황희찬(30·울버햄튼 원더러스)이 PSV아인트호벤과 연결됐다. 네덜란드 매체 '사커뉴스'는 16일(한국시간) &quot;PSV가 황희찬을 주시하고 있다. 오랫동안 몸상태를 지켜봐 왔다&quot;며 &quot;현재 PSV는 알라산 플레아... ",
                                                        "commentCount": 0,
                                                        "viewCount": 0,
                                                        "viewedByMe": false
                                                    },
                                                    {
                                                        "id": "40b8dadd-7cc5-40e8-9e9e-c923e2af6d36",
                                                        "source": "NAVER",
                                                        "sourceUrl": "http://www.hansbiz.co.kr/news/articleView.html?idxno=809300",
                                                        "title": "'韓 유일 부자 월드컵 출전' 차범근·차두리 &quot;언젠간 트로피 들어올리길...",
                                                        "publishDate": "2026-01-16T16:14:00",
                                                        "summary": "사상 최대 규모(48개국)로 열리는 이번 대회에선 홍명보 감독을 중심으로 손흥민, 이강인, 김민재, 황희찬 등 역대 최고 수준의 멤버를 앞세워 선전을 다짐한다. 차범근은 &quot;다 똑같은 마음일 것이다. 대표팀이 이번... ",
                                                        "commentCount": 0,
                                                        "viewCount": 0,
                                                        "viewedByMe": false
                                                    }
                                                ],
                                                "nextCursor": "2026-01-16T16:14",
                                                "nextAfter": "2026-01-19T01:34:33.735912Z",
                                                "size": 5,
                                                "totalElements": 189,
                                                "hasNext": true
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )

    })
    ResponseEntity<CursorPageResponseArticleDto> getArticles(UUID userId, ArticleRequestDto request);

    @Operation(summary = "기사 단건 조회", description = "뉴스 기사 ID로 뉴스 기사 단건을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ArticleDto.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "id": "a25c0b80-cd5c-4bd8-b313-6127a62da859",
                                                "source": "NAVER",
                                                "sourceUrl": "https://www.sportalkorea.com/news/articleView.html?idxno=2025052909552907062",
                                                "title": "&quot;이적 가능성 두고 접촉 시작&quot;...&quot;손흥민 이탈, 공격진 공백&quot; 토트넘, 진...",
                                                "publishDate": "2026-01-19T12:33:00",
                                                "summary": "손흥민의 빈자리를 케빈 샤데가 채울 수 있을까. 영국 매체 '풋볼팬캐스트'는 19일(한국시간) &quot;토트넘... 중심에는 손흥민이 있었다&quot;고 운을 뗐다. 이어 &quot;손흥민은 2015년 여름 바이어 04 레버쿠젠에서 토트넘으로 이적한... ",
                                                "commentCount": 0,
                                                "viewCount": 0,
                                                "viewedByMe": false
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "기사 정보 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "timestamp": "2026-01-19T04:11:02.906172900Z",
                                                "code": "ARTICLE_NOT_EXIST",
                                                "message": "Article is not exist",
                                                "details": {
                                                    "articleId": "4cbd83ea-9eef-4f20-a3b1-909704bb67ba"
                                                },
                                                "executionType": "ArticleNotExistException",
                                                "status": 404
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    ResponseEntity<ArticleDto> getArticle(UUID userId, UUID articleId);

    @Operation(summary = "기사 논리 삭제", description = "기사를 논리적으로 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "논리 삭제 성공"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "기사 정보 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "timestamp": "2026-01-19T04:11:02.906172900Z",
                                                "code": "ARTICLE_NOT_EXIST",
                                                "message": "Article is not exist",
                                                "details": {
                                                    "articleId": "4cbd83ea-9eef-4f20-a3b1-909704bb67ba"
                                                },
                                                "executionType": "ArticleNotExistException",
                                                "status": 404
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )

    })
    ResponseEntity<Void> deleteArticleSoft(UUID articleId);

    @Operation(summary = "기사 물리 삭제", description = "기사를 물리적으로 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "물리 삭제 성공"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "기사 정보 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "timestamp": "2026-01-19T04:11:02.906172900Z",
                                                "code": "ARTICLE_NOT_EXIST",
                                                "message": "Article is not exist",
                                                "details": {
                                                    "articleId": "4cbd83ea-9eef-4f20-a3b1-909704bb67ba"
                                                },
                                                "executionType": "ArticleNotExistException",
                                                "status": 404
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    ResponseEntity<Void> deleteArticleHard(UUID articleId);

    @Operation(summary = "기사 뷰 등록", description = "기사 뷰를 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "기사 뷰 등록 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ArticleViewDto.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "id": "4cbd83ea-9eef-4f20-a3b1-909704bb67ba",
                                                "viewedBy": "cc3781f3-ac75-4a6e-9fd6-745615ec1bd0",
                                                "createdAt": "2026-01-19T04:04:46.633220500Z",
                                                "articleId": "151b52db-3e96-46ce-b13b-c023dd4fc349",
                                                "source": "NAVER",
                                                "sourceUri": "https://www.spotvnews.co.kr/news/articleView.html?idxno=793914",
                                                "articleTitle": "[오피셜] &quot;손흥민 벽화 그려줬잖아! 야유 좀 그만해&quot; 토트넘이 밝힌 진...",
                                                "articlePublishedDate": "2026-01-19T11:53:00",
                                                "articleSummary": "홈에서 야유와 시위까지 일어나자 '손흥민'의 이름까지 꺼냈다. 구단 경영진은 팬들과의 소통을 강조하며 과거 손흥민의 벽화를 제작했던 사례를 증거로 제시했지만, 싸늘하게 식은 여론을 되돌릴 수 있을지 미지수다.... ",
                                                "articleCommentCount": 0,
                                                "articleViewCount": 1
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    ResponseEntity<ArticleViewDto> recordArticleView(UUID userId, UUID articleId);

    @Operation(summary = "출처 목록 조회", description = "출처 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = List.class),
                            examples = @ExampleObject(
                                    value = """
                                            [
                                                "NAVER",
                                                "HANKYUNG",
                                                "CHOSUN",
                                                "YEONHAP"
                                            ]
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    ResponseEntity<List<String>> getSources();

    @Operation(summary = "뉴스 복구", description = "유실된 뉴스 기사를 복구합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "복구 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ArticleRestoreResultDto.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "restoreDate": "2026-01-19T13:20:39.7047943",
                                                "restoredArticleIds": [
                                                    "40f98823-2d6f-4ceb-9c9d-2e91aad9c500"
                                                ],
                                                "restoredArticleCount": 1
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    ResponseEntity<ArticleRestoreResultDto> restoreArticles(LocalDateTime from, LocalDateTime to);
}

