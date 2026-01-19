package com.example.monew.domain.interest.dto;

import java.util.List;
import java.util.UUID;

public record InterestDto(
        UUID id,
        String name,
        List<String> keywords,
        Long subscriberCount,
        Boolean subscribedByMe
)
{}