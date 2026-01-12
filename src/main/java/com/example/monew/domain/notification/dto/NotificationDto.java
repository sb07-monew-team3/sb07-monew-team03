package com.example.monew.domain.notification.dto;

import com.example.monew.domain.notification.entity.Notifications;
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
    public static CursorResponse<NotificationDto> dtoCursorResponse(Slice<NotificationDto> sliceDto) {
        String nextCursorUUID = null;
        Instant nextCreatedAt = null;

        if (!sliceDto.getContent().isEmpty()) {
            NotificationDto notificationDto = sliceDto.getContent().get(sliceDto.getContent().size() - 1);
            nextCursorUUID = notificationDto.id().toString();
            nextCreatedAt = notificationDto.createdAt();
        }

        return new CursorResponse<>(sliceDto.getContent(),
            nextCursorUUID,
            nextCreatedAt,
            sliceDto.getSize(),
            null,
            sliceDto.hasNext());
    }

    public static NotificationDto toDto(Notifications noti) {
        return NotificationDto.builder()
            .id(noti.getId())
            .createdAt(noti.getCreatedAt())
            .updatedAt(noti.getUpdatedAt())
            .confirmed(noti.isRead())
            .userId(noti.getUser().getId())
            .content(noti.getContent())
            .resourceType(noti.getResourceType())
            .resourceId(noti.getResourceId())
            .build();
    }
}
