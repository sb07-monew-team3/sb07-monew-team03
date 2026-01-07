package com.example.monew.global.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class CommonExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> customExceptionHandler(CustomException e){

        int statusCode = e.errorCode.getStatusCode();
        ErrorResponse errorResponse = new ErrorResponse(
                e.timestamp,
                e.errorCode.name(),
                e.errorCode.getMessage(),
                e.details,
                e.getClass().getSimpleName(),
                e.errorCode.getStatusCode()
        );
        return ResponseEntity.status(statusCode).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> methodArgumentExceptionHandler(MethodArgumentNotValidException e){

        Map<String,Object> details = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(
                fieldError -> details.put(fieldError.getField(), fieldError.getDefaultMessage())
        );
        int statusCode = ErrorCode.ARGUMENT_VALID_FAIL.getStatusCode();
        ErrorResponse errorResponse = new ErrorResponse(
                Instant.now(),
                ErrorCode.ARGUMENT_VALID_FAIL.name(),
                ErrorCode.ARGUMENT_VALID_FAIL.getMessage(),
                details,
                e.getClass().getSimpleName(),
                statusCode
        );
        return ResponseEntity.status(statusCode).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> restExceptionHandler(Exception e){
        int statusCode = ErrorCode.INTERNAL_SERVER_ERROR.getStatusCode();
        ErrorResponse errorResponse = new ErrorResponse(

                Instant.now(),
                ErrorCode.INTERNAL_SERVER_ERROR.name(),
                ErrorCode.INTERNAL_SERVER_ERROR.getMessage(),
                null,
                e.getClass().getSimpleName(),
                statusCode
        );
        return ResponseEntity.status(statusCode).body(errorResponse);
    }
}
