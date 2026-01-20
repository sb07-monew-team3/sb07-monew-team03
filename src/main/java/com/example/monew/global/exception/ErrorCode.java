package com.example.monew.global.exception;

public enum ErrorCode {

    USER_EMAIL_EXIST("Email already exist",409),
    INTERNAL_SERVER_ERROR("Internal Server Error",500),
    USER_VALID_FAIL("User validation fail",401),
    ARGUMENT_VALID_FAIL ("Invalid argument",400),
    USER_NOT_EXIST("User is not exist",404),
    NOTIFICATION_NOT_EXIST("##### NOTI is not exist", 404),
    INVALID_SEARCH_CONDITION("Invalid search condition", 400),
    ARTICLE_NOT_EXIST("Article is not exist",404),
    INTEREST_NOT_EXIST("Interest is not exist", 404),
    INTEREST_DUPLICATE_NAME("Duplicate or similar interest name", 409),
    SUBSCRIPTION_NOT_EXIST("Subscription is not exist", 404),
    LOG_NOT_EXIST("Log is not exist", 404);


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
