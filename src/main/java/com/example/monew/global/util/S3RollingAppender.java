package com.example.monew.global.util;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.time.LocalDate;

@RequiredArgsConstructor
public class S3RollingAppender extends RollingFileAppender <ILoggingEvent>{

    private final S3LogStorage s3LogStorage;
    @Override
    public void rollover(){

        super.rollover();
        LocalDate yesterday = LocalDate.now().minusDays(1);
        String rolledFileName = String.format("logs/app.%s.log", yesterday);
        File rolledFile = new File(rolledFileName);
        if (rolledFile.exists()) {
            s3LogStorage.uploadS3(rolledFile.getPath());
        }

    }

}
