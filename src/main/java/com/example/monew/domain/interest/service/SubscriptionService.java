package com.example.monew.domain.interest.service;

import com.example.monew.domain.interest.dto.SubscriptionDto;

import java.util.List;
import java.util.UUID;

public interface SubscriptionService {

    SubscriptionDto subscribe(UUID interestId, UUID userId);
    void unsubscribe(UUID interestId, UUID userId);
    List<UUID> getSubscribedInterestIds(UUID interestId);
}
