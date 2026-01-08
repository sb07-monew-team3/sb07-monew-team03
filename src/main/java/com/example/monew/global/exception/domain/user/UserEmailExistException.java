package com.example.monew.global.exception.domain.user;

import com.example.monew.global.exception.CustomException;
import com.example.monew.global.exception.ErrorCode;

import java.util.HashMap;

public class UserEmailExistException extends CustomException {

    public UserEmailExistException(String email) {
        super(ErrorCode.USER_EMAIL_EXIST,
                new HashMap<>(){
                    {put("existEmail", email);}
                }

        );
    }
}
