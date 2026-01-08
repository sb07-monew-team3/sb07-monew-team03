package com.example.monew.domain.activity.dto;

import java.time.Instant;
import java.util.UUID;

public record UserActivityDto(

        UUID id,
        String email,
        String nickname,
        Instant createdAt

) {
}
