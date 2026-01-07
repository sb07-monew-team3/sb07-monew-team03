package com.example.monew.global.exception;

public enum ErrorCode {

    USER_EMAIL_EXIST("Email already exist",409),
    INTERNAL_SERVER_ERROR("Internal Server Error",500),
    ARGUMENT_VALID_FAIL ("Invalid argument",400);

    String message;
    int statusCode;
    ErrorCode(String message,int statusCode) {
        this.message = message;
        this.statusCode = statusCode;
    }
    public String getMessage(){
        return this.message;
    }
    public int getStatusCode(){
        return this.statusCode;
    }
}
