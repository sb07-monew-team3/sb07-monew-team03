package com.example.monew.domain.article.dto;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Arrays;

public enum Order {
    PUBLISH_DATE("publishDate"),
    COMMENT_COUNT("commentCount"),
    VIEW_COUNT("viewCount");

    private final String value;

    Order(String value) {
        this.value = value;
    }

    @JsonCreator
    public static Order forValue(String value) {
        return Arrays.stream(values())
                .filter(order -> order.value.equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("invalid order value"));
    }
}
