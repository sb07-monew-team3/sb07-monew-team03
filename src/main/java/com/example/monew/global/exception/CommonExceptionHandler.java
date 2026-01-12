package com.example.monew.global.exception;

import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

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

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleMethodValidationException(HandlerMethodValidationException e
    ) {
        ErrorResponse errorResponse = new ErrorResponse(
            Instant.now(),
            "INVALID_REQUEST",
            "$$$$$ 잘못된 요청",
            null,
            e.getClass().getSimpleName(),
            HttpStatus.BAD_REQUEST.value()
        );

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(errorResponse);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingRequestHeader(MissingRequestHeaderException e) {
        ErrorResponse errorResponse = new ErrorResponse(
            Instant.now(),
            "INVALID_REQUEST",
            "$$$$$ 필수 요청 헤더 누락",
            Map.of(
                "headerName", e.getHeaderName()
            ),
            e.getClass().getSimpleName(),
            HttpStatus.BAD_REQUEST.value()
        );

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(errorResponse);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException e) {
        ErrorResponse errorResponse = new ErrorResponse(
            Instant.now(),
            ErrorCode.ARGUMENT_VALID_FAIL.name(),
            ErrorCode.ARGUMENT_VALID_FAIL.getMessage(),
            null,
            e.getClass().getSimpleName(),
            HttpStatus.BAD_REQUEST.value()
        );

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(errorResponse);
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
