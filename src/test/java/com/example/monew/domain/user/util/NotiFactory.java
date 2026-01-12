package com.example.monew.domain.user.util;

import com.example.monew.domain.user.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class NotiFactory {

    @PersistenceContext
    private EntityManager em;

    String randomString(){
        return UUID.randomUUID().toString().substring(0,10);
    }

    public User newUser(){
        return new User(randomString()+"@naver.com", randomString(), randomString(),null);
    }

    public UUID newNoti(User user, String content, int daysAgo, boolean isRead) {

        UUID id = UUID.randomUUID();
        UUID resourceId = UUID.randomUUID();
        Instant createdAt = Instant.now().minus(daysAgo, ChronoUnit.DAYS);

        em.createNativeQuery(
                "INSERT INTO notifications " +
                    "(id, user_id, content, resource_type, resource_id, is_read, created_at, updated_at) " +
                    "VALUES (:id, :userId, :content, :resourceType, :resourceId, :isRead, :createdAt, :updatedAt)")
            .setParameter("id", id)
            .setParameter("userId", user.getId())
            .setParameter("content", content)
            .setParameter("resourceType", "INTEREST")  // ResourceType enum 값
            .setParameter("resourceId", resourceId)
            .setParameter("isRead", isRead)
            .setParameter("createdAt", createdAt)
            .setParameter("updatedAt", createdAt)  // created_at과 동일하게 설정
            .executeUpdate();

        return id;
    }


}

