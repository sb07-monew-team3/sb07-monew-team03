package com.example.monew.global.exception.domain.article;

import com.example.monew.global.exception.CustomException;
import com.example.monew.global.exception.ErrorCode;

import java.util.HashMap;

public class InvalidSearchConditionException extends CustomException {
    public InvalidSearchConditionException(String message) {
        super(ErrorCode.INVALID_SEARCH_CONDITION, new HashMap<>(){
            {put("message", message);}
        });
    }
}
