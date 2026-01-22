package com.example.monew.domain.notification.entity;

import com.example.monew.domain.base.BaseCreatableEntity;
import com.example.monew.domain.interest.entity.Interest;
import com.example.monew.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Table(name = "notifications")
@EntityListeners(AuditingEntityListener.class)
public class Notifications extends BaseCreatableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", columnDefinition = "uuid")
    @OnDelete(action = OnDeleteAction.CASCADE)
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

    @Setter(AccessLevel.PROTECTED)
    @LastModifiedDate
    @Column(columnDefinition = "timestamp with time zone")
    private Instant updatedAt;

    public Notifications(
        User user,
        String content,
        ResourceType resourceType,
        UUID resourceId
    ) {
        this.user = user;
        this.content = content;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.isRead = false;
    }

    public void checkNotificationRead(UUID userId) {
        if(userId != null && this.user.getId().equals(userId)) {
            this.isRead = true;
        }
    }

    public String toString(){
        return "##### Notifications{" +
//                "id =" + getId() +
//                ", updatedAt=" + updatedAt +
                ", content='" + content + '\'' +
                ", createdAt=" + super.getCreatedAt() +
                ", isRead=" + isRead +
                '}';
    }
}
