package com.example.monew.domain.interest.repository;

import com.example.monew.domain.interest.entity.Subscription;

import java.util.List;
import java.util.UUID;

public interface SubscriptionRepositoryCustom {

    List<Subscription> getSubscriptionByUserId(UUID userId);
    Long countByInterestId(UUID interestId);
}
