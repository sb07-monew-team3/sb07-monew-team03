package com.example.monew.domain.article.client.naver;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NaverNewsResponse(
        String lastBuildDate,
        int total,
        int start,
        int display,
        List<Item> items
) { }
