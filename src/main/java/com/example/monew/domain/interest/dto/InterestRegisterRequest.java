package com.example.monew.domain.interest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record InterestRegisterRequest (
        @NotBlank(message = "관심사 이름은 필수입니다.")
        String name,

        @NotEmpty(message = "키워드는 최소 1개가 필요합니다.")
        List<String> keywords
)
{}
