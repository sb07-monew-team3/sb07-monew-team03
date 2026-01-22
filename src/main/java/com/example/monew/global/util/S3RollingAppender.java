package com.example.monew.global.util;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


public class S3RollingAppender extends RollingFileAppender <ILoggingEvent>{

    private S3LogStorage s3LogStorage; // setter로 주입

    public S3RollingAppender() {
        super(); // 기본 생성자 필수
    }

    public void setS3LogStorage(S3LogStorage s3LogStorage) {
        this.s3LogStorage = s3LogStorage;
    }

    @Value("${logging.file.path}")
    String logPath;
    @Override
    public void rollover(){

        super.rollover();
        LocalDate yesterday = LocalDate.now().minusDays(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String rolledFileName = String.format( logPath+ "/app-%s.log", yesterday.format(formatter));
        File rolledFile = new File(rolledFileName);
        if (rolledFile.exists()) {
            s3LogStorage.uploadS3(rolledFile.getPath());
        }

    }

}
