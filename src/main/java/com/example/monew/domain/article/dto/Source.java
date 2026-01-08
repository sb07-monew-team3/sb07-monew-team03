package com.example.monew.domain.article.dto;

import lombok.Getter;

@Getter
public enum Source {
    NAVER("NAVER"),
    HANKYUNG("HANKYUNG"),
    CHOSUN("CHOSUN"),
    YEONHAP("YEONHAP");

    private final String value;

    Source(String value) {
        this.value = value;
    }
}
