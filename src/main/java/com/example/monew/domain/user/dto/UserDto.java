package com.example.monew.domain.user.dto;

import jakarta.validation.Valid;

import java.time.Instant;
import java.util.UUID;

@Valid
public record UserDto(
        UUID id,
        String email,
        String nickname,
        Instant createdAt
) {
}
