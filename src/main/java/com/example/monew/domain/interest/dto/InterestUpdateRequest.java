package com.example.monew.domain.interest.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record InterestUpdateRequest(

        @NotEmpty
        List<String> keywords
)
{}
