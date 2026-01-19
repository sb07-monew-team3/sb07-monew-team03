package com.example.monew.global.exception.domain.user;

import com.example.monew.global.exception.CustomException;
import com.example.monew.global.exception.ErrorCode;

import java.util.HashMap;

public class UserValidationFailException extends CustomException {
    public UserValidationFailException(String email) {
        super(ErrorCode.USER_VALID_FAIL, new HashMap<>(){
            { put("email", email);}
                }

        );
    }
}
