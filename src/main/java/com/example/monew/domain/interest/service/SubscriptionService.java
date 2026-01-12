package com.example.monew.domain.interest.service;

import com.example.monew.domain.interest.dto.SubscriptionDto;

import java.util.UUID;

public interface SubscriptionService {

    SubscriptionDto subscribe(UUID interestId, UUID userId);
    void unsubscribe(UUID interestId, UUID userId);
}
