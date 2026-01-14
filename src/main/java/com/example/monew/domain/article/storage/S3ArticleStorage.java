package com.example.monew.domain.article.storage;

import com.example.monew.domain.article.entity.Article;
import com.example.monew.domain.article.repository.ArticleRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class S3ArticleStorage {

    static final long BATCH_INTERVAL = 1000 * 24 * 60 * 60;

    private S3Client s3Client;

    @Value("${s3.bucket}")
    private String bucketName;
    @Value("${s3.access-key}")
    private String accessKey;
    @Value("${s3.secret-key}")
    private String secretKey;
    @Value("${s3.region}")
    private String region;

    private final ArticleRepository articleRepository;

    @PostConstruct
    private void initializeAmazonS3Client() {

        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }

    @Scheduled(fixedRate = BATCH_INTERVAL)
    @Transactional
    public void backupArticles() {
        try {

            LocalDate yesterday = LocalDate.now().minusDays(1); // 하루마다 어제자 뉴스들을 백업
            LocalDateTime start = yesterday.atTime(LocalTime.MIN);
            LocalDateTime end = yesterday.atTime(23, 59, 59);

            List<Article> articleList = articleRepository.findAllByPublishDateBetween(start, end);

            ObjectMapper objectMapper = new ObjectMapper()
                    .registerModule(new JavaTimeModule()) // Instant, LocalDateTime 지원
                    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

            String json = objectMapper.writeValueAsString(articleList);

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key("articles/" + yesterday + ".json")
                    .contentType("application/json")
                    .build();

            s3Client.putObject(
                    putObjectRequest,
                    RequestBody.fromString(json)
            );

        } catch (JsonProcessingException e) {
            // TODO: 커스텀 예외 추가
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public List<Article> loadArticlesFromBackup(LocalDateTime from, LocalDateTime to) {

        List<Article> articleList = new ArrayList<>();

        LocalDate fromDate = from.toLocalDate();
        LocalDate toDate = to.toLocalDate();

        while(fromDate.isBefore(toDate) || fromDate.isEqual(toDate)) {
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key("articles/" + fromDate + ".json")
                    .build();

            try (ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(request)){
                String json = new String(s3Object.readAllBytes(), StandardCharsets.UTF_8);

                ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

                List<Article> articles = objectMapper.readValue(json, new TypeReference<List<Article>>() {});
                articleList.addAll(articles);

            } catch (NoSuchKeyException e) {
                log.info("S3 backup file not found: {}", fromDate);
            } catch (IOException e) {
                // TODO: 커스텀 예외 추가
                throw new RuntimeException(e);
            }

            fromDate = fromDate.plusDays(1);
        }

        return articleList;
    }
}
