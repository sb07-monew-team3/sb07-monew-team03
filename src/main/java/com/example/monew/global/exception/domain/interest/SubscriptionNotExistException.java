package com.example.monew.global.exception.domain.interest;

import com.example.monew.global.exception.CustomException;
import com.example.monew.global.exception.ErrorCode;

import java.util.Map;
import java.util.UUID;

public class SubscriptionNotExistException extends CustomException {

    public SubscriptionNotExistException(UUID userId, UUID interestId) {
        super(ErrorCode.SUBSCRIPTION_NOT_EXIST, Map.of("userId", userId, "interestId", interestId));
    }
}
