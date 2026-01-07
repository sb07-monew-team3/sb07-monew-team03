package com.example.monew.global.exception;

public enum ErrorCode {

    USER_EMAIL_EXIST("Email already exist"),
    INTERNAL_SERVER_ERROR("Internal Server Error"),
    ARGUMENT_VALID_FAIL ("Invalid argument");

    String message;
    ErrorCode(String message) {
        this.message = message;
    }
    public String getMessage(){
        return this.message;
    }
}
