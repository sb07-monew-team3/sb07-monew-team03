package com.example.monew.global.exception;

import java.time.Instant;
import java.util.Map;

public abstract class CustomException extends RuntimeException {

    final Instant timestamp = Instant.now();
    final ErrorCode errorCode;
    final Map<String, Object> details;

    public CustomException(ErrorCode errorCode, Map<String, Object> details) {
        this.errorCode = errorCode;
        this.details = details;
    }
}
