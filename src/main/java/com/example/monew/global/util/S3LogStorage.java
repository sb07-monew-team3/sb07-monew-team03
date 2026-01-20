package com.example.monew.global.util;

import com.example.monew.global.exception.domain.s3.S3LogNotExistException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class S3LogStorage {



    private S3Client s3Client;
    private S3Presigner s3Presigner;

    @Value("${s3.bucket}")
    private String bucketName;
    @Value("${s3.access-key}")
    private String accessKey;
    @Value("${s3.secret-key}")
    private String secretKey;
    @Value("${s3.region}")
    private String region;
    private final int EXPIRATION_SECONDS = 60;  //url 지속시간 60초


    @PostConstruct
    private void initializeAWSS3(){
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();

        this.s3Presigner = S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }

    public String put(String fileName,byte[] bytes){
        String logFileName = "logs/" + fileName;
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(logFileName)
                .contentType("text/plain")
                .build();
        s3Client.putObject(
                request,
                RequestBody.fromBytes(bytes)
        );

        return fileName;
    }

    public void uploadS3(String filePath){
        File file = new File(filePath);
        String fileName = file.getName();

        try {
            byte[] bytes = Files.readAllBytes(file.toPath());
            put(fileName,bytes);
        } catch (IOException e) {
            throw new S3LogNotExistException(file);
        }
    }

}
