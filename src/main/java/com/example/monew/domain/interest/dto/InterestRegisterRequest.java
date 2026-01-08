package com.example.monew.domain.interest.dto;

import java.util.List;

public record InterestRegisterRequest (
        String name,
        List<String> keywords
)
{}
