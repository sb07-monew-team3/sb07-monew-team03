package com.example.monew.global.exception.domain.user;

import com.example.monew.global.exception.CustomException;
import com.example.monew.global.exception.ErrorCode;

import java.util.HashMap;
import java.util.UUID;

public class UserNotExistException extends CustomException {

    public UserNotExistException(UUID userId) {
        super(ErrorCode.USER_NOT_EXIST, new HashMap<>(){
            {put("userId", userId);}
        });
    }
}
