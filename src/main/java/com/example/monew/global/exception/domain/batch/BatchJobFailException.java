package com.example.monew.global.exception.domain.batch;

import com.example.monew.global.exception.CustomException;
import com.example.monew.global.exception.ErrorCode;

import java.util.Map;

public class BatchJobFailException extends CustomException {

    public BatchJobFailException(String message) {
        super(ErrorCode.BATCH_JOB_FAIL, Map.of("Batch Fail", message));
    }
}
