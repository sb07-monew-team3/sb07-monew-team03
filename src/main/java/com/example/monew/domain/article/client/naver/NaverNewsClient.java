package com.example.monew.domain.article.client.naver;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class NaverNewsClient {

    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://openapi.naver.com")
            .build();

    @Value("${naver.client-id}")
    private String clientId;

    @Value("${naver.client-secret}")
    private String clientSecret;

    public NaverNewsResponse search(String keyword) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/search/news.json")
                        .queryParam("query", keyword) // 검색어
                        .queryParam("display", 10) // 기사를 몇개 가져올지
                        .queryParam("sort", "date") // 정렬 기준 (날짜)
                        .build())
                .header("X-Naver-Client-Id", clientId)
                .header("X-Naver-Client-Secret", clientSecret)
                .retrieve()
                .body(NaverNewsResponse.class);
    }
}
