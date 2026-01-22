package com.example.monew.domain.activity.docs;

import com.example.monew.domain.activity.dto.UserActivityDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.bson.Document;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@Tag(name = "UserActivity", description = "UserActivity API")
public interface UserActivityControllerDocs {

    @Operation(summary = "유저 활동 내역 조회")
    @ApiResponse(
            responseCode = "200",
            description = "유저 활동 내역 조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserActivityDto.class),
                    examples = @ExampleObject(
                            name = "유저 활동 내역 조회 성공",
                            value = """
                                    {
                                        "id": "6297d535-4f8d-48df-8a0e-67db876105a6",
                                        "email": "test123@gmail.com",
                                        "nickname": "test123",
                                        "createdAt": "2026-01-06T00:23:45.387723Z",
                                        "subscriptions": [
                                            {
                                                "id": "eae6ecc6-4f08-4b2f-9260-f3b60f8849ae",
                                                "interestId": "624524c2-926c-4964-9138-6862eb2506f8",
                                                "interestName": "삼성",
                                                "interestKeywords": [
                                                    "samsung"
                                                ],
                                                "interestSubscriberCount": 3,
                                                "createdAt": "2026-01-09T05:16:46.778866Z"
                                            }
                                        ],
                                        "comments": [
                                            {
                                                "id": "c50b9ab9-d932-4a32-ba0d-643535c9d22c",
                                                "articleId": "97f3b4a3-53d9-4dfd-aa92-782b40f6d0be",
                                                "articleTitle": "브이파이브 게임즈, '미르의 전설 2 레드나이트' 정식 출시",
                                                "userId": "6297d535-4f8d-48df-8a0e-67db876105a6",
                                                "userNickname": "hwempire",
                                                "content": "내꺼",
                                                "likeCount": 0,
                                                "createdAt": "2026-01-12T04:44:17.604371Z"
                                            },
                                            {
                                                "id": "8373458a-7c8f-46ee-88e5-97c6bee9df29",
                                                "articleId": "99e28806-80f6-4f87-b859-177ac491e18b",
                                                "articleTitle": "'흥민이 형 보고 싶었어' <b>손흥민</b> 만난 LAFC 동료들 함박웃음…'개막 한 ...",
                                                "userId": "6297d535-4f8d-48df-8a0e-67db876105a6",
                                                "userNickname": "hwempire",
                                                "content": "생성용",
                                                "likeCount": 0,
                                                "createdAt": "2026-01-12T04:40:14.807672Z"
                                            },
                                            {
                                                "id": "3aaeea68-506e-4521-a49e-13f9f31140dc",
                                                "articleId": "7111b1cf-af73-4c99-bb49-1b3703200d08",
                                                "articleTitle": "‘<b>손흥민</b> 도와주러 왔는데…’ 입단하자마자 수술대 오른다, ‘오피셜’...",
                                                "userId": "6297d535-4f8d-48df-8a0e-67db876105a6",
                                                "userNickname": "hwempire",
                                                "content": "황준영이 작성한 댓글임",
                                                "likeCount": 1,
                                                "createdAt": "2026-01-12T04:26:12.363320Z"
                                            },
                                            {
                                                "id": "d6f5a883-fdbb-48ec-8c14-0e166b85cd9e",
                                                "articleId": "443d79e6-e774-4d91-ae33-b8bd09d53d03",
                                                "articleTitle": "용인FC, 포은아트홀서 창단식…K<b>리그</b>2 출사표",
                                                "userId": "6297d535-4f8d-48df-8a0e-67db876105a6",
                                                "userNickname": "hwempire",
                                                "content": "로날도 is goat",
                                                "likeCount": 0,
                                                "createdAt": "2026-01-06T02:35:26.583158Z"
                                            },
                                            {
                                                "id": "93aa7b79-c9c8-431a-9d36-641b8ee861c9",
                                                "articleId": "443d79e6-e774-4d91-ae33-b8bd09d53d03",
                                                "articleTitle": "용인FC, 포은아트홀서 창단식…K<b>리그</b>2 출사표",
                                                "userId": "6297d535-4f8d-48df-8a0e-67db876105a6",
                                                "userNickname": "hwempire",
                                                "content": "siuu",
                                                "likeCount": 0,
                                                "createdAt": "2026-01-06T02:35:19.237330Z"
                                            }
                                        ],
                                        "commentLikes": [
                                            {
                                                "id": "2f7688a3-ce58-4165-835c-480e85a4faed",
                                                "createdAt": "2026-01-12T04:26:46.475165Z",
                                                "commentId": "3aaeea68-506e-4521-a49e-13f9f31140dc",
                                                "articleId": "7111b1cf-af73-4c99-bb49-1b3703200d08",
                                                "articleTitle": "‘<b>손흥민</b> 도와주러 왔는데…’ 입단하자마자 수술대 오른다, ‘오피셜’...",
                                                "commentUserId": "6297d535-4f8d-48df-8a0e-67db876105a6",
                                                "commentUserNickname": "hwempire",
                                                "commentContent": "황준영이 작성한 댓글임",
                                                "commentLikeCount": 1,
                                                "commentCreatedAt": "2026-01-12T04:26:12.363320Z"
                                            },
                                            {
                                                "id": "0589e50b-4d2c-45d5-b5cc-82a6166bdb31",
                                                "createdAt": "2026-01-06T06:02:01.141498Z",
                                                "commentId": "7a2e5253-8505-4e9e-ab13-97f1484eb324",
                                                "articleId": "28065805-7cf2-41e7-96da-25df02fdcb9b",
                                                "articleTitle": "용인FC 공식 창단...구단주 이상일 시장 &quot;미래는 우리가 만들어 가는 것...",
                                                "commentUserId": "65f98993-99a6-4297-b0db-610eb821159f",
                                                "commentUserNickname": "우디",
                                                "commentContent": "ㅁㄴㅇㄹㅁㄴㅇㄹ",
                                                "commentLikeCount": 2,
                                                "commentCreatedAt": "2026-01-06T05:58:59.877100Z"
                                            },
                                            {
                                                "id": "557aef06-34f4-48ee-9430-8db76fee7188",
                                                "createdAt": "2026-01-06T06:01:36.818409Z",
                                                "commentId": "bf42c153-cf21-4504-9d3c-bcc65127446b",
                                                "articleId": "28065805-7cf2-41e7-96da-25df02fdcb9b",
                                                "articleTitle": "용인FC 공식 창단...구단주 이상일 시장 &quot;미래는 우리가 만들어 가는 것...",
                                                "commentUserId": "8cbdfb82-5d47-45fe-a4de-c51a565f7d69",
                                                "commentUserNickname": "TEST",
                                                "commentContent": "ㅎㅇㅇㅇ",
                                                "commentLikeCount": 3,
                                                "commentCreatedAt": "2026-01-06T04:06:26.880817Z"
                                            },
                                            {
                                                "id": "c90bf385-63b8-4d93-9176-540e570b0ead",
                                                "createdAt": "2026-01-06T06:01:29.155403Z",
                                                "commentId": "748c1e0b-b165-428b-8bcc-0251b7048d9b",
                                                "articleId": "28065805-7cf2-41e7-96da-25df02fdcb9b",
                                                "articleTitle": "용인FC 공식 창단...구단주 이상일 시장 &quot;미래는 우리가 만들어 가는 것...",
                                                "commentUserId": "8cbdfb82-5d47-45fe-a4de-c51a565f7d69",
                                                "commentUserNickname": "TEST",
                                                "commentContent": "좋아요 한 번씩만 부탁드립니다",
                                                "commentLikeCount": 4,
                                                "commentCreatedAt": "2026-01-06T05:23:03.693558Z"
                                            },
                                            {
                                                "id": "1a178d4b-6554-4f1b-9f7f-18100d91e1b2",
                                                "createdAt": "2026-01-06T06:01:27.484812Z",
                                                "commentId": "3398d7d0-8e64-4e2a-997a-585ff6de00fe",
                                                "articleId": "28065805-7cf2-41e7-96da-25df02fdcb9b",
                                                "articleTitle": "용인FC 공식 창단...구단주 이상일 시장 &quot;미래는 우리가 만들어 가는 것...",
                                                "commentUserId": "830eabad-1b7e-4154-a49c-2b4804ad7314",
                                                "commentUserNickname": "우디",
                                                "commentContent": "ㅇㅇ",
                                                "commentLikeCount": 3,
                                                "commentCreatedAt": "2026-01-06T05:25:13.568467Z"
                                            },
                                            {
                                                "id": "5e4cf27c-212b-48ee-af72-5ccf22b5ab66",
                                                "createdAt": "2026-01-06T05:25:37.338255Z",
                                                "commentId": "5827dbf8-1678-41f1-946b-7c601057d7b5",
                                                "articleId": "28065805-7cf2-41e7-96da-25df02fdcb9b",
                                                "articleTitle": "용인FC 공식 창단...구단주 이상일 시장 &quot;미래는 우리가 만들어 가는 것...",
                                                "commentUserId": "75e7bbfb-80e6-46eb-af67-cc6bab4d52c3",
                                                "commentUserNickname": "우디",
                                                "commentContent": "dd",
                                                "commentLikeCount": 4,
                                                "commentCreatedAt": "2026-01-06T00:18:32.992930Z"
                                            },
                                            {
                                                "id": "a9e754cf-2198-45e6-8ab7-2c6362e27e59",
                                                "createdAt": "2026-01-06T05:25:36.498188Z",
                                                "commentId": "f02b3f40-9f2e-485d-975a-0185be8d1add",
                                                "articleId": "28065805-7cf2-41e7-96da-25df02fdcb9b",
                                                "articleTitle": "용인FC 공식 창단...구단주 이상일 시장 &quot;미래는 우리가 만들어 가는 것...",
                                                "commentUserId": "8cbdfb82-5d47-45fe-a4de-c51a565f7d69",
                                                "commentUserNickname": "TEST",
                                                "commentContent": "ㅇㅇㅇ",
                                                "commentLikeCount": 2,
                                                "commentCreatedAt": "2026-01-06T04:06:25.036693Z"
                                            },
                                            {
                                                "id": "6002d82a-0425-4077-a300-8522f3a20d71",
                                                "createdAt": "2026-01-06T05:25:35.160232Z",
                                                "commentId": "603d1229-8ba8-40ca-a44d-73dee5397a80",
                                                "articleId": "28065805-7cf2-41e7-96da-25df02fdcb9b",
                                                "articleTitle": "용인FC 공식 창단...구단주 이상일 시장 &quot;미래는 우리가 만들어 가는 것...",
                                                "commentUserId": "13c9f0ea-051c-42b9-ba8d-d0764985bfd9",
                                                "commentUserNickname": "집가고싶어",
                                                "commentContent": "ㅂㅂㅂㅂㅂ",
                                                "commentLikeCount": 4,
                                                "commentCreatedAt": "2026-01-06T04:27:44.282839Z"
                                            },
                                            {
                                                "id": "e6e427ff-4185-41c5-b629-97f8ede20cf0",
                                                "createdAt": "2026-01-06T05:25:34.472782Z",
                                                "commentId": "adbf84db-94ec-4d38-86aa-ab10f1289b0a",
                                                "articleId": "28065805-7cf2-41e7-96da-25df02fdcb9b",
                                                "articleTitle": "용인FC 공식 창단...구단주 이상일 시장 &quot;미래는 우리가 만들어 가는 것...",
                                                "commentUserId": "13c9f0ea-051c-42b9-ba8d-d0764985bfd9",
                                                "commentUserNickname": "집가고싶어",
                                                "commentContent": "테스팅",
                                                "commentLikeCount": 5,
                                                "commentCreatedAt": "2026-01-06T04:33:13.800230Z"
                                            }
                                        ],
                                        "articleViews": [
                                            {
                                                "id": "6d839ca4-f6dc-4534-af5f-d575d72e221d",
                                                "viewedBy": "6297d535-4f8d-48df-8a0e-67db876105a6",
                                                "createdAt": "2026-01-15T09:12:28.901863Z",
                                                "articleId": "c920404c-a948-4473-ad98-310ba9b78351",
                                                "source": "NAVER",
                                                "sourceUrl": "https://www.tenasia.co.kr/article/2026011385774",
                                                "articleTitle": "'공개열애 2번' 전현무, 결국 저격 당했다…&quot;별 여자 없다, 머리 말고 마...",
                                                "articlePublishedDate": "2026-01-13T23:59:00",
                                                "articleSummary": "'혼자는 못 해' 전현무<b>가</b> 선우용여에게 결혼 잔소리를 들었다. 13일 방송된 JTBC 신규 예능 '혼자는 못 해'에서는 전현무, 추성훈, 이수지, 이세희와 함께한 선우용여의 찜질방 투어<b>가</b> 공개됐다. 이날 선우용여는 추성훈을... ",
                                                "articleCommentCount": 33,
                                                "articleViewCount": 6
                                            },
                                            {
                                                "id": "f8fccb22-7b73-401e-83d1-f1124745eaa7",
                                                "viewedBy": "6297d535-4f8d-48df-8a0e-67db876105a6",
                                                "createdAt": "2026-01-12T04:40:09.618263Z",
                                                "articleId": "99e28806-80f6-4f87-b859-177ac491e18b",
                                                "source": "NAVER",
                                                "sourceUrl": "https://www.fourfourtwo.co.kr/news/articleView.html?idxno=85811",
                                                "articleTitle": "'흥민이 형 보고 싶었어' <b>손흥민</b> 만난 LAFC 동료들 함박웃음…'개막 한 ...",
                                                "articlePublishedDate": "2026-01-11T11:35:00",
                                                "articleSummary": "<b>손흥민</b>을 비롯한 선수들은 가장 먼저 북중미카리브해<b>축구</b>연맹(CONCACAF) 챔피언스컵 일정에 나선다.... MLS 사무국은 <b>손흥민</b>과 <b>메시</b>의 대결 구도를 통해 팬들의 이목을 집중시키고자 했다. 경기장 역시 양 팀 홈구장이... ",
                                                "articleCommentCount": 0,
                                                "articleViewCount": 1
                                            },
                                            {
                                                "id": "604f81c3-e7b4-478a-9a0e-c5bae8e11880",
                                                "viewedBy": "6297d535-4f8d-48df-8a0e-67db876105a6",
                                                "createdAt": "2026-01-12T04:36:08.340581Z",
                                                "articleId": "edf878cd-ebfb-40af-867c-ddb01c2ecf3f",
                                                "source": "NAVER",
                                                "sourceUrl": "https://www.sedaily.com/NewsView/2K79G0X9UG",
                                                "articleTitle": "3500개 법인서 수십조 유입…&quot;투자비중 제한은 아쉬워&quot; 지적도",
                                                "articlePublishedDate": "2026-01-11T17:34:00",
                                                "articleSummary": "비트<b>코인</b> 현물 상장지수펀드(ETF) 출시에도 속도가 붙을 것으로 전망된다. 11일 <b>금융</b>계에 따르면... 업계 관계자는 “과거 보험사의 <b>주식</b> 투자 제한도 20년 전에 폐지됐다”며 “가상화폐에만 과도한 규제를 적용하면... ",
                                                "articleCommentCount": 0,
                                                "articleViewCount": 4
                                            },
                                            {
                                                "id": "e60043b1-1bb4-45eb-8368-4351d98c82c6",
                                                "viewedBy": "6297d535-4f8d-48df-8a0e-67db876105a6",
                                                "createdAt": "2026-01-12T04:26:05.692867Z",
                                                "articleId": "7111b1cf-af73-4c99-bb49-1b3703200d08",
                                                "source": "NAVER",
                                                "sourceUrl": "https://www.goal.com/kr/뉴스/a/blt6c784f809c761393",
                                                "articleTitle": "‘<b>손흥민</b> 도와주러 왔는데…’ 입단하자마자 수술대 오른다, ‘오피셜’...",
                                                "articlePublishedDate": "2026-01-10T17:49:00",
                                                "articleSummary": "‘해양의 <b>메시</b>’라고 부르기도 한다. 이런 그는 새 시즌 ‘흥부 듀오’ <b>손흥민</b>과 부앙가로 이어지는... 로스앤젤레스 FC는 내달 3일 북중미카리브<b>축구</b>연맹(CONCACAF) 챔피언스컵 1라운드를 시작으로 새 시즌 일정을... ",
                                                "articleCommentCount": 0,
                                                "articleViewCount": 2
                                            },
                                            {
                                                "id": "30d9f2a7-cc7b-46ff-a47f-62bbd2b1e6ca",
                                                "viewedBy": "6297d535-4f8d-48df-8a0e-67db876105a6",
                                                "createdAt": "2026-01-12T01:10:56.546444Z",
                                                "articleId": "a03f3528-9f27-44d5-8f94-058e09e7dc76",
                                                "source": "NAVER",
                                                "sourceUrl": "http://enews.imbc.com/News/RetrieveNewsInfo/490364",
                                                "articleTitle": "남미 거친 몸싸움에 ‘히든FC’ 선수들 속수무책 “상당히 위험해 보여...",
                                                "articlePublishedDate": "2026-01-10T16:02:00",
                                                "articleSummary": "‘히든FC’는 <b>손흥민</b>&amp;김민재가 활약했던 독일, <b>메시</b>의 조국이자 <b>축구</b> 강국인 아르헨티나, 그리고 ‘2024년 실버컵 준우승’을 쟁취한 미국&amp;캐나다 연합 팀이 있는 ‘죽음의 조’에 편성됐다. 해설위원 현영민은... ",
                                                "articleCommentCount": 0,
                                                "articleViewCount": 2
                                            },
                                            {
                                                "id": "d0bd3ffa-893c-45c4-abcc-85c4f325c677",
                                                "viewedBy": "6297d535-4f8d-48df-8a0e-67db876105a6",
                                                "createdAt": "2026-01-08T07:03:51.775629Z",
                                                "articleId": "d7af1635-ca36-447d-8e1e-9f5e938e8c67",
                                                "source": "NAVER",
                                                "sourceUrl": "https://www.siminilbo.co.kr/news/newsview.php?ncode=1160287102427550",
                                                "articleTitle": "<b>삼성</b>전자, CES 2026서 '<b>삼성</b> 기술 포럼' 진행",
                                                "articlePublishedDate": "2026-01-07T16:50:00",
                                                "articleSummary": "▲ <b>삼성</b>전자가 세계 최대 전자 전시회 CES 2026에서 '<b>삼성</b> 기술 포럼(<b>Samsung</b> Tech Forum)'을 갖고 AI 시대에 발맞춘 기술 디자인의 새로운 방향성으로 '인간 중심 디자인(Human Centered Design)' 비전을 제시했다.... ",
                                                "articleCommentCount": 0,
                                                "articleViewCount": 1
                                            },
                                            {
                                                "id": "df50c991-4eb0-48dd-8378-cb116e998bed",
                                                "viewedBy": "6297d535-4f8d-48df-8a0e-67db876105a6",
                                                "createdAt": "2026-01-06T06:25:01.146920Z",
                                                "articleId": "d750d039-3db6-43dc-94c0-ed2f4975060f",
                                                "source": "NAVER",
                                                "sourceUrl": "https://www.obsnews.co.kr/news/articleView.html?idxno=1509567",
                                                "articleTitle": "용인FC 창단…&quot;2030년 K<b>리그</b>1 승격 목표&quot;",
                                                "articlePublishedDate": "2026-01-05T20:40:00",
                                                "articleSummary": "올해부터 K<b>리그</b>2에 참가하는 용인FC가 공식 창단식을 열고 2030년 K<b>리그</b>1 승격과 아시아<b>축구</b>연맹 <b>챔피언스리그</b> 도전을 목표로 제시했습니다. 최윤겸 감독과 이동국 기술감독은 석현준과 김민우 등 새롭게 구성한 선수... ",
                                                "articleCommentCount": 0,
                                                "articleViewCount": 1
                                            },
                                            {
                                                "id": "f675a1b6-da10-4625-93d1-310f8df59f2c",
                                                "viewedBy": "6297d535-4f8d-48df-8a0e-67db876105a6",
                                                "createdAt": "2026-01-06T06:24:51.177132Z",
                                                "articleId": "08ef2cc5-2385-4a67-b731-3b6e2bed7a88",
                                                "source": "NAVER",
                                                "sourceUrl": "http://www.joynews24.com/view/1924580",
                                                "articleTitle": "용인FC 공식 출범…이상일 시장 “미래는 우리가 만들어 가는 것”",
                                                "articlePublishedDate": "2026-01-05T21:16:00",
                                                "articleSummary": "[사진=용인시] 용인FC는 페어플레이 정신을 바탕으로 승리하는 팀, 시민에게 즐거움과 행복을 주는 팀을 비전으로 제시하고 2030년 K<b>리그</b>1 승격과 아시아<b>축구</b>연맹(AFC) <b>챔피언스리그</b> 진출을 목표로 했다. 이날 이 시장과... ",
                                                "articleCommentCount": 0,
                                                "articleViewCount": 1
                                            },
                                            {
                                                "id": "b11cab1b-bce0-47a9-adc5-8282023e53e6",
                                                "viewedBy": "6297d535-4f8d-48df-8a0e-67db876105a6",
                                                "createdAt": "2026-01-06T06:01:18.126630Z",
                                                "articleId": "5bdb6dd7-eda5-40e1-ab77-f374f32294c5",
                                                "source": "NAVER",
                                                "sourceUrl": "https://www.mk.co.kr/article/11462792",
                                                "articleTitle": "훈련하는 <b>야구</b> 국가대표팀 [MK포토]",
                                                "articlePublishedDate": "2025-11-07T19:34:00",
                                                "articleSummary": "7일 고척스카이돔에서 ‘2025 NAVER K-BASEBALL SERIES’ 국가대표팀 훈련이 진행됐다. 한국 대표팀은 오는 8일과 9일 고척돔에서 체코와 두 차례 평가전을 갖고, 이어서 대표팀은 도쿄돔에서 일본과 평가전 두... ",
                                                "articleCommentCount": 0,
                                                "articleViewCount": 1
                                            },
                                            {
                                                "id": "52868a61-ab32-425a-9e4e-169195f6526f",
                                                "viewedBy": "6297d535-4f8d-48df-8a0e-67db876105a6",
                                                "createdAt": "2026-01-06T06:01:12.196442Z",
                                                "articleId": "bb821fc7-5297-471e-bfcc-5bb50d052d98",
                                                "source": "NAVER",
                                                "sourceUrl": "http://www.globalepic.co.kr/view.php?ud=202511051748384175ac3d53c8ec_29",
                                                "articleTitle": "컴투스, ‘2025 NAVER K-BASEBALL SERIES’ 공식 후원",
                                                "articlePublishedDate": "2025-11-05T19:36:00",
                                                "articleSummary": "컴투스(대표 남재관)가 KBO와 함께 대한민국 <b>야구</b> 대표팀 평가전인 ‘2025 NAVER K-BASEBALL SERIES’를 공식... 컴투스는 지난해에 이어 2년 연속 대한민국 <b>야구</b> 대표팀 평가전을 후원하며, 대한민국 <b>야구</b>의 경쟁력... ",
                                                "articleCommentCount": 0,
                                                "articleViewCount": 1
                                            }
                                        ]
                                    }
                                    """
                    )
            )
    )
    @GetMapping("/{userId}")
    ResponseEntity<UserActivityDto> getUserActivity(@PathVariable("userId") UUID userId);
}
