package com.example.monew.global.exception.domain.interest;

import com.example.monew.global.exception.CustomException;
import com.example.monew.global.exception.ErrorCode;

import java.util.Map;

public class InterestDuplicateNameException extends CustomException {

    public InterestDuplicateNameException(String name) {
        super(ErrorCode.INTEREST_DUPLICATE_NAME, Map.of("name", name));
    }
}
