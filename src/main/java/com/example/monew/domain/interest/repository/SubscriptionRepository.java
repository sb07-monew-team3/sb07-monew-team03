package com.example.monew.domain.interest.repository;

import com.example.monew.domain.interest.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID>, SubscriptionRepositoryCustom {

    @Query("SELECT s FROM Subscription s " +
            "WHERE s.user.id = :userId AND s.interest.id = :interestId")
    Optional<Subscription> findSubscription(UUID userId, UUID interestId);

    @Query("SELECT COUNT(s) > 0 FROM Subscription s " +
            "WHERE s.user.id = :userId AND s.interest.id = :interestId")
    boolean isSubscribed(@Param("userId") UUID userId,
                         @Param("interestId") UUID interestId);
}
