package com.example.monew.global.exception.domain.interest;

import com.example.monew.global.exception.CustomException;
import com.example.monew.global.exception.ErrorCode;

import java.util.Map;
import java.util.UUID;

public class InterestNotExistException extends CustomException {
    public InterestNotExistException(UUID interestId) {
        super(ErrorCode.INTEREST_NOT_EXIST, Map.of("interestId", interestId));
    }
}
