package com.example.monew.domain.notification.dto;

import com.example.monew.domain.notification.entity.ResourceType;
import com.example.monew.domain.notification.response.CursorResponse;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import org.springframework.data.domain.Slice;

@Builder
public record NotificationDto(
    UUID id,
    Instant createdAt,
    Instant updatedAt,
    boolean confirmed,
    UUID userId,
    String content,
    ResourceType resourceType,
    UUID resourceId
) {
    public static CursorResponse<NotificationDto> from(Slice<NotificationDto> sliceDto, String uuidString, Instant ceatedAt) {
        return new CursorResponse<>(sliceDto.getContent(),
            uuidString,
            ceatedAt,
            sliceDto.getSize(),
            null,
            sliceDto.hasNext());
    }
}
