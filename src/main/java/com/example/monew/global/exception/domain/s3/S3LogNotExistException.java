package com.example.monew.global.exception.domain.s3;

import com.example.monew.global.exception.CustomException;
import com.example.monew.global.exception.ErrorCode;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class S3LogNotExistException extends CustomException {
    public S3LogNotExistException(File file) {
        super(ErrorCode.LOG_NOT_EXIST, Map.of("fileName", file.getName() ) );
    }
}
