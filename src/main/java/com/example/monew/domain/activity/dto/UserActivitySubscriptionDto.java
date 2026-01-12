package com.example.monew.domain.activity.dto;

import java.time.Instant;
import java.util.UUID;

public record UserActivitySubscriptionDto(
        UUID id,
        UUID interestId,
        String interestName,
        String[] interestKeywords,
        int interestSubscriberCount,
        Instant createdAt

) {
}
