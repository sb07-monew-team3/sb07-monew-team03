package com.example.monew.domain.activity.mapper;

import com.example.monew.domain.activity.dto.*;
import com.example.monew.domain.article.dto.ArticleViewDto;
import org.bson.Document;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Comment;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Component
public class UserActivityMapper {

    public UserActivityDto toUserActivityDto(Document document) {

        Document articleViewDoc = (Document) document.get("articleViews");
        Document commentDoc = (Document) document.get("comments");
        Document subscriptionDoc = (Document) document.get("subscriptions");
        Document commentLikeDoc = (Document) document.get("commentsLikes");
        List<UserActivityArticleViewDto> userActivityArticleViewDtos = new ArrayList<>();
        List<UserActivityCommentLikeDto> userActivityCommentLikeDtos = new ArrayList<>();
        List<UserActivityCommentDto> userActivityCommentDtos = new ArrayList<>();
        List<UserActivitySubscriptionDto> userActivitySubscriptionDtos= new ArrayList<>();
        for (String key : articleViewDoc.keySet()) {
            if (key == null || key.isBlank()) continue;
            Document subDoc = (Document) articleViewDoc.get(key);
            UserActivityArticleViewDto userActivityArticleViewDto = new UserActivityArticleViewDto(
                    UUID.fromString(key),
                    UUID.fromString(subDoc.getString("viewedBy")),
                    Instant.parse(subDoc.getString("createdAt")),
                    UUID.fromString(subDoc.getString("articleId")),
                    subDoc.getString("source"),
                    subDoc.getString("sourceUrl"),
                    subDoc.getString("articleTitle"),
                    LocalDateTime.parse(subDoc.getString("articlePublishedDate")),
                    subDoc.getString("articleSummary"),
                    subDoc.getInteger("articleCommentCount"),
                    subDoc.getInteger("articleViewCount")
            );
            userActivityArticleViewDtos.add(userActivityArticleViewDto);
        }

        for (String key : commentDoc.keySet()) {
            if (key == null || key.isBlank()) continue;
            Document subDoc = (Document) commentDoc.get(key);
            UserActivityCommentDto userActivityCommentDto = new UserActivityCommentDto(
                    UUID.fromString(key),
                    UUID.fromString(subDoc.getString("articleId")),
                    subDoc.getString("articleTitle"),
                    UUID.fromString(subDoc.getString("userId")),
                    subDoc.getString("userNickname"),
                    subDoc.getString("content"),
                    subDoc.getInteger("likeCount"),
                    Instant.parse(subDoc.getString("createdAt"))

            );
            userActivityCommentDtos.add(userActivityCommentDto);
        }

        for (String key : subscriptionDoc.keySet()) {
            if (key == null || key.isBlank()) continue;
            Document subDoc = (Document) subscriptionDoc.get(key);
            List<String> interestKeywords =
                    subDoc.get("interestKeywords") == null
                            ? new ArrayList<>()
                            : (List<String>) subDoc.get("interestKeywords");
            UserActivitySubscriptionDto userActivitySubscriptionDto = new UserActivitySubscriptionDto(
                    UUID.fromString(key),
                    UUID.fromString(subDoc.getString("interestId")),
                    subDoc.getString("interestName"),
                    interestKeywords.toArray(new String[0]),
                    subDoc.getInteger("interestSubscriberCount"),
                    Instant.parse(subDoc.getString("createdAt"))
            );
            userActivitySubscriptionDtos.add(userActivitySubscriptionDto);
        }

        for(String key : commentLikeDoc.keySet()){
            if (key == null || key.isBlank()) continue;
            Document subDoc = (Document) commentLikeDoc.get(key);
            UserActivityCommentLikeDto userActivityCommentLikeDto = new UserActivityCommentLikeDto(
                    UUID.fromString(key),
                    Instant.parse(subDoc.getString("createdAt")),
                    UUID.fromString(subDoc.getString("commentId")),
                    UUID.fromString(subDoc.getString("articleId")),
                    subDoc.getString("articleTitle"),
                    UUID.fromString(subDoc.getString("commentUserId")),
                    subDoc.getString("commentUserNickname"),
                    subDoc.getString("commentContent"),
                    subDoc.getInteger("commentLikeCount"),
                    Instant.parse(subDoc.getString("commentCreatedAt"))
            );
            userActivityCommentLikeDtos.add(userActivityCommentLikeDto);
        }
        UserActivitySubscriptionDto[] subscriptionArray = userActivitySubscriptionDtos.stream()
                .sorted(Comparator.comparing(UserActivitySubscriptionDto::createdAt, Comparator.reverseOrder()))
                .toArray(UserActivitySubscriptionDto[]::new);
        UserActivityCommentDto[] commentArray = userActivityCommentDtos.stream()
                .sorted(Comparator.comparing(UserActivityCommentDto::createdAt, Comparator.reverseOrder()))
                .limit(10)
                .toArray(UserActivityCommentDto[]::new);
        UserActivityCommentLikeDto[] commentLikesArray = userActivityCommentLikeDtos.stream()
                .sorted(Comparator.comparing(UserActivityCommentLikeDto::createdAt, Comparator.reverseOrder()))
                .limit(10)
                .toArray(UserActivityCommentLikeDto[]::new);
        UserActivityArticleViewDto[] articleViewArray = userActivityArticleViewDtos.stream()
                .sorted(Comparator.comparing(UserActivityArticleViewDto::createdAt, Comparator.reverseOrder()))
                .limit(10)
                .toArray(UserActivityArticleViewDto[]::new);

        return new UserActivityDto(
                UUID.fromString(document.getString("id")),
                document.getString("email"),
                document.getString("nickname"),
                Instant.parse(document.getString("createdAt")),
                subscriptionArray,
                commentArray,
                commentLikesArray,
                articleViewArray
        );
    }
}
