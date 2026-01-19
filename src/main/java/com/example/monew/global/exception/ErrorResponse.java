package com.example.monew.global.exception;

import java.time.Instant;
import java.util.Map;

public record ErrorResponse(
        Instant timestamp,
        String code,
        String message,
        Map<String, Object> details,
        String executionType,
        int status
) {

}
