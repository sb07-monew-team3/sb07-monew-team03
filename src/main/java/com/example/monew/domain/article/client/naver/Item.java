package com.example.monew.domain.article.client.naver;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Item(
        String title,
        String originallink,
        String link,
        String description,
        String pubDate
) {}
