package com.example.monew.global.exception.domain.comment;

import com.example.monew.global.exception.CustomException;
import com.example.monew.global.exception.ErrorCode;

import java.util.Map;

public class CommentInvalidRequestException extends CustomException {

    public CommentInvalidRequestException(Map<String, Object> details) {
        super(ErrorCode.ARGUMENT_VALID_FAIL, details);
    }

    public static CommentInvalidRequestException of(String reason) {
        return new CommentInvalidRequestException(Map.of("reason", reason));
    }
}
