package com.example.monew.domain.user.dto;

import jakarta.validation.constraints.NotBlank;

public record UserUpdateRequest(
        @NotBlank
        String nickname
) {
}
