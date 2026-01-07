package com.example.monew.domain.notification.entity;

import com.example.monew.domain.base.BaseCreatableEntity;
import com.example.monew.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;
import java.time.LocalDateTime;
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

    private String content;

    @Enumerated(EnumType.STRING)
    private ResourceType resourceType;

    private UUID resourceId;

    private boolean isRead;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

}
