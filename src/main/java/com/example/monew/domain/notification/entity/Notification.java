package com.example.monew.domain.notification.entity;

import com.example.monew.domain.base.BaseCreatableEntity;
import com.example.monew.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;
import java.util.UUID;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Table(name = "notifications")
public class Notification extends BaseCreatableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name="content",nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResourceType resourceType;

    @Column(name = "resource_id", columnDefinition = "uuid", nullable = false)
    private UUID resourceId;

    @Column(name="is_read",nullable = false)
    @ColumnDefault( "false")
    private boolean isRead;

    @LastModifiedDate
    @Column(columnDefinition = "timestamp with time zone")
    private Instant updatedAt;

    public void checkNotificationRead(UUID userId) {
        if(this.user.getId().equals(userId)) {
            this.isRead = true;
        }
    }
}
