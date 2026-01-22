package com.example.monew.domain.activity.service;

import com.example.monew.domain.activity.dto.*;
import com.example.monew.domain.comment.dto.CommentResponse;
import com.example.monew.domain.interest.dto.InterestDto;
import com.example.monew.domain.user.dto.UserDto;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class IMongoDbService implements MongoDbService {

    private final MongoClient mongoClient;

    @Override
    public void mongoConnection() {
        MongoDatabase database = mongoClient.getDatabase("admin");
        database.runCommand(new Document("ping", 1));
        log.info("MongoDb Connection Success");
    }

    @Override
    public void insertUserActivity(UserDto userDto) {
        MongoCollection<Document> collection = getCollection("userActivity");
        String userActivityKey = userDto.id().toString();
        Document doc = new Document()
                .append("email", userDto.email())
                .append("nickname", userDto.nickname())
                .append("createdAt", userDto.createdAt().toString())
                .append("subscriptions", new ArrayList<>())
                .append("comments", new ArrayList<>())
                .append("commentsLikes", new ArrayList<>())
                .append("articleViews", new ArrayList<>());

        collection.updateOne(
                Filters.exists("UserActivity"),
                Updates.set("UserActivity." + userActivityKey, doc)
        );
    }

    @Override
    public void updateUserActivityComment(CommentResponse commentResponse) {
        MongoCollection<Document> collection = getCollection("userActivity");
        collection.updateOne(
                Filters.exists("Comment." + commentResponse.getId().toString()),
                Updates.set("Comment." + commentResponse.getId().toString() + ".content", commentResponse.getContent())
        );
        collection.updateOne(
                Filters.exists("CommentLike." + commentResponse.getId().toString()),
                Updates.set("CommentLike." + commentResponse.getId().toString() + ".commentContent", commentResponse.getContent())
        );
    }

    @Override
    public void insertUserActivityCommentLike(UUID userId, UserActivityCommentLikeDto commentLikeDto) {
        MongoCollection<Document> collection = getCollection("userActivity");
        String commentLikeKey = commentLikeDto.commentId().toString();
        Document commentLike = new Document()
                .append("createdAt", commentLikeDto.createdAt().toString())
                .append("commentId", commentLikeDto.commentId().toString())
                .append("articleId", commentLikeDto.articleId().toString())
                .append("articleTitle", commentLikeDto.articleTitle())
                .append("commentUserId", commentLikeDto.commentUserId().toString())
                .append("commentUserNickname", commentLikeDto.commentUserNickname())
                .append("commentContent", commentLikeDto.commentContent())
                .append("commentLikeCount", commentLikeDto.commentLikeCount())
                .append("commentCreatedAt", commentLikeDto.commentCreatedAt().toString());

        collection.updateOne(
                Filters.exists("UserActivity." + userId.toString()),
                Updates.push("UserActivity." + userId.toString() + ".commentsLikes", commentLikeKey)
        );

        collection.updateOne(
                Filters.and(
                        Filters.exists("CommentLike"),
                        Filters.not(Filters.exists("CommentLike." + commentLikeKey))
                ),
                Updates.set("CommentLike." + commentLikeKey,commentLike)
        );
        collection.updateOne(
                Filters.exists("CommentLike." + commentLikeKey),
                Updates.inc("CommentLike." + commentLikeKey + ".commentLikeCount", 1)
        );
        collection.updateOne(
                Filters.exists("Comment." + commentLikeDto.commentId().toString()),
                Updates.inc("Comment." + commentLikeDto.commentId().toString() + ".likeCount", 1)
        );
    }

    @Override
    public void insertUserActivityComment(UUID userId, UserActivityCommentDto commentDto) {
        MongoCollection<Document> collection = getCollection("userActivity");
        String commentKey = commentDto.id().toString();
        Document comment = new Document()
                .append("id", commentDto.id().toString())
                .append("articleId", commentDto.articleId().toString())
                .append("articleTitle", commentDto.articleTitle())
                .append("userId", commentDto.userId().toString())
                .append("userNickname", commentDto.userNickname())
                .append("content", commentDto.content())
                .append("likeCount", commentDto.likeCount())
                .append("createdAt", commentDto.createdAt().toString());
        collection.updateOne(
                Filters.exists("Comment"),
                Updates.set("Comment." + commentKey, comment)
        );
        collection.updateOne(
                Filters.exists("UserActivity." + userId.toString()),
                Updates.push("UserActivity." + userId.toString() + ".comments", commentKey)
        );
        collection.updateOne(
                Filters.exists("ArticleView." + commentDto.articleId().toString()),
                Updates.inc("ArticleView." + commentDto.articleId().toString() + ".articleCommentCount", 1)
        );
    }

    @Override
    public void insertUserActivitySubscription(UUID userId, UserActivitySubscriptionDto subscriptionDto) {
        MongoCollection<Document> collection = getCollection("userActivity");
        String subscriptionKey = subscriptionDto.interestId().toString();
        Document subscription = new Document()
                .append("interestId", subscriptionDto.interestId().toString())
                .append("interestName", subscriptionDto.interestName())
                .append("interestKeywords", Arrays.asList(subscriptionDto.interestKeywords()))
                .append("interestSubscriberCount", subscriptionDto.interestSubscriberCount())
                .append("createdAt", subscriptionDto.createdAt().toString());
        collection.updateOne(
                Filters.and(
                        Filters.exists("Subscription"),
                        Filters.not(Filters.exists("Subscription." + subscriptionKey))
                ),
                Updates.set("Subscription." + subscriptionKey,subscription)
        );
        collection.updateOne(
                Filters.exists("Subscription"+ subscriptionKey),
                Updates.inc("Subscription." + subscriptionKey + ".interestSubscriberCount", 1)
        );

        collection.updateOne(
                Filters.exists("UserActivity." + userId.toString()),
                Updates.push("UserActivity." + userId.toString() + ".subscriptions", subscriptionKey)
        );
    }

    @Override
    public void insertUserActivityArticleView(UUID userId, UserActivityArticleViewDto articleViewDto) {
        MongoCollection<Document> collection = getCollection("userActivity");
        String articleKey = articleViewDto.articleId().toString();
        Document articleView = new Document()
                .append("viewedBy", articleViewDto.viewedBy().toString())
                .append("createdAt", articleViewDto.createdAt().toString())
                .append("articleId", articleViewDto.articleId().toString())
                .append("source", articleViewDto.source())
                .append("sourceUrl", articleViewDto.sourceUrl())
                .append("articleTitle", articleViewDto.articleTitle())
                .append("articleSummary", articleViewDto.articleSummary())
                .append("articleCommentCount", articleViewDto.articleCommentCount())
                .append("articleViewCount", articleViewDto.articleViewCount())
                .append("articlePublishedDate", articleViewDto.articlePublishedDate().toString());
        collection.updateOne(
                Filters.and(
                        Filters.exists("ArticleView"),
                        Filters.not(Filters.exists("ArticleView." + articleKey))
                ),
                        Updates.set("ArticleView." + articleKey,articleView)
        );
        collection.updateOne(
                Filters.exists("UserActivity." + userId.toString()),
                Updates.push("UserActivity." + userId.toString() + ".articleViews", articleKey)
        );
        collection.updateOne(
                Filters.exists("ArticleView." + articleKey),
                Updates.inc("ArticleView." + articleKey + ".articleViewCount", 1)
        );
    }

    @Override
    public void updateUserActivity(UserDto userDto) {
        MongoCollection<Document> collection = getCollection("userActivity");
        collection.updateOne(
                Filters.exists("UserActivity." + userDto.id().toString()),
                Updates.set("UserActivity." + userDto.id().toString() + ".nickname", userDto.nickname())
        );
    }

    @Override
    public void updateWhenUnCommentLike(UUID userId, UUID commentId) {
        MongoCollection<Document> collection = getCollection("userActivity");
        collection.updateOne(
                Filters.exists("UserActivity." + userId.toString()),
                Updates.pull("UserActivity." + userId.toString() + ".commentsLikes", commentId.toString())
        );

        collection.updateOne(
                Filters.exists("comment." + commentId.toString()),
                Updates.inc("comment." + commentId.toString() + ".likeCount", -1)
        );

        collection.updateOne(
                Filters.exists("CommentLike." +commentId.toString()),
                Updates.inc("CommentLike." + commentId.toString() + ".commentLikeCount", -1)
        );
    }

    @Override
    public void updateWhenUnSubscription(UUID interestId, UUID userId) {
        MongoCollection<Document> collection = getCollection("userActivity");
        collection.updateOne(
                Filters.exists("UserActivity." + userId.toString()),
                Updates.pull("UserActivity." + userId.toString() + ".subscriptions", interestId.toString())
        );
        collection.updateOne(
                Filters.exists("Subscription." + interestId.toString()),
                Updates.inc("Subscription." + interestId.toString() + ".interestSubscriberCount", -1)
        );
    }

    @Override
    public void updateWhenCommentDelete(UUID articleId) {
        MongoCollection<Document> collection = getCollection("userActivity");
        collection.updateOne(
                Filters.exists("ArticleView." + articleId.toString()),
                Updates.inc("ArticleView." + articleId.toString() + ".articleCommentCount", -1)
        );
    }

    @Override
    public void updateSubscription(InterestDto interestDto) {
        MongoCollection<Document> collection = getCollection("userActivity");
        collection.updateOne(
                Filters.exists("Subscription." + interestDto.id().toString()),
                Updates.set("Subscription." + interestDto.id().toString() + ".interestKeywords", interestDto.keywords())
        );
    }
    @Override
    public void updateWhenSubscriptionDelete(UUID interestId, List<UUID> userIds){
        MongoCollection<Document> collection = getCollection("userActivity");
        userIds.forEach(userId -> collection.updateOne(
                Filters.exists("UserActivity." + userId.toString()),
                Updates.pull("UserActivity." + userId.toString() + ".subscriptions", interestId.toString())
        ));
        collection.updateOne(
                Filters.exists("Subscription." + interestId.toString()),
                Updates.unset("Subscription." + interestId.toString())
        );
    }

    @Override
    public UserActivityDto getUserActivity(UUID userId) {
        MongoCollection<Document> collection = getCollection("userActivity");
        Document userActivityDoc = collection.find(
                        Filters.exists("UserActivity." + userId.toString())
                )
                .projection(Projections.include("UserActivity." + userId.toString()))
                .first();
        Document subscriptionDoc = (Document) collection.find(
                Filters.exists("Subscription")
        ).first().get("Subscription");
        Document commetDoc = (Document) collection.find(
                Filters.exists("Comment")
        ).first().get("Comment");
        Document articleViewDoc = (Document) collection.find(
                Filters.exists("ArticleView")
        ).first().get("ArticleView");
        Document commentLikeDoc = (Document) collection.find(
                Filters.exists("CommentLike")
        ).first().get("CommentLike");

        Document userActivityMap = (Document) userActivityDoc.get("UserActivity");
        Document userActivityTarget = (Document) userActivityMap.get(userId.toString());


        List<UUID> subscriptionIds = userActivityTarget.getList("subscriptions", String.class).stream().map(UUID::fromString).toList();
        List<UUID> commentIds = userActivityTarget.getList("comments", String.class).stream().map(UUID::fromString).toList();
        List<UUID> commentLikeIds = userActivityTarget.getList("commentsLikes", String.class).stream().map(UUID::fromString).toList();
        List<UUID> articleViewIds = userActivityTarget.getList("articleViews", String.class).stream().map(UUID::fromString).toList();

        List<UserActivityCommentLikeDto> commentLikeDtos = new ArrayList<>();
        List<UserActivityCommentDto> commentDtos = new ArrayList<>();
        List<UserActivitySubscriptionDto> subscriptionDtos = new ArrayList<>();
        List<UserActivityArticleViewDto> articleViewDtos = new ArrayList<>();

        subscriptionIds.stream().forEach(
                x -> {
                    Document targetDoc = (Document) subscriptionDoc.get(x.toString());
                    List<String> keywords =
                            targetDoc.get("interestKeywords") == null
                            ? new ArrayList<>()
                            : (List<String>) targetDoc.get("interestKeywords");
                    log.info("keywords : " + keywords.toString());
                    subscriptionDtos.add(new UserActivitySubscriptionDto(
                                    x,
                                    UUID.fromString(targetDoc.getString("interestId")),
                                    targetDoc.getString("interestName"),
                            keywords.toArray(String[]::new),
                                    targetDoc.getInteger("interestSubscriberCount"),
                                    Instant.parse(targetDoc.getString("createdAt"))

                            )
                    );
                    return;
                }
        );

        commentIds.stream().forEach(
                x -> {
                    Document targetDoc = (Document) commetDoc.get(x.toString());
                    commentDtos.add(new UserActivityCommentDto(
                                    x,
                                    UUID.fromString(targetDoc.getString("articleId")),
                                    targetDoc.getString("articleTitle"),
                                    UUID.fromString(targetDoc.getString("userId")),
                                    targetDoc.getString("userNickname"),
                                    targetDoc.getString("content"),
                                    targetDoc.getInteger("likeCount"),
                                    Instant.parse(targetDoc.getString("createdAt"))
                            )
                    );
                    return;
                }

        );

        commentLikeIds.stream().forEach(
                x -> {
                    Document targetDoc = (Document) commentLikeDoc.get(x.toString());
                    commentLikeDtos.add(new UserActivityCommentLikeDto(
                                    x,
                                    Instant.parse(targetDoc.getString("createdAt")),
                                    UUID.fromString(targetDoc.getString("commentId")),
                                    UUID.fromString(targetDoc.getString("articleId")),
                                    targetDoc.getString("articleTitle"),
                                    UUID.fromString(targetDoc.getString("commentUserId")),
                                    targetDoc.getString("commentUserNickname"),
                                    targetDoc.getString("commentContent"),
                                    targetDoc.getInteger("commentLikeCount"),
                                    Instant.parse(targetDoc.getString("commentCreatedAt"))
                            )
                    );
                    return;
                }
        );
        articleViewIds.stream().forEach(
                x -> {
                    Document targetDoc = (Document) articleViewDoc.get(x.toString());
                    articleViewDtos.add(
                            new UserActivityArticleViewDto(
                                    x,
                                    UUID.fromString(targetDoc.getString("viewedBy")),
                                    Instant.parse(targetDoc.getString("createdAt")),
                                    UUID.fromString(targetDoc.getString("articleId")),
                                    targetDoc.getString("source"),
                                    targetDoc.getString("sourceUrl"),
                                    targetDoc.getString("articleTitle"),
                                    LocalDateTime.parse(targetDoc.getString("articlePublishedDate")),
                                    targetDoc.getString("articleSummary"),
                                    targetDoc.getInteger("articleCommentCount"),
                                    targetDoc.getInteger("articleViewCount")
                            )
                    );
                    return;
                }
        );
        List<UserActivitySubscriptionDto> subscriptionResult = subscriptionDtos.stream()
                .sorted(Comparator.comparing(UserActivitySubscriptionDto::createdAt).reversed())
                .toList();
        List<UserActivityCommentDto> commentResult = commentDtos.stream()
                .sorted(Comparator.comparing(UserActivityCommentDto::createdAt).reversed())
                .limit(10)
                .toList();
        List<UserActivityCommentLikeDto> commentLikeResult = commentLikeDtos.stream()
                .sorted(Comparator.comparing(UserActivityCommentLikeDto::createdAt).reversed())
                .limit(10)
                .toList();
        List<UserActivityArticleViewDto> articleViewResult = articleViewDtos.stream()
                .sorted(Comparator.comparing(UserActivityArticleViewDto::createdAt).reversed())
                .limit(10)
                .toList();

        return new UserActivityDto(
                userId,
                userActivityTarget.getString("email"),
                userActivityTarget.getString("nickname"),
                Instant.parse(userActivityTarget.getString("createdAt")),
                subscriptionResult.toArray(new UserActivitySubscriptionDto[0]),
                commentResult.toArray(new UserActivityCommentDto[0]),
                commentLikeResult.toArray(new UserActivityCommentLikeDto[0]),
                articleViewResult.toArray(new UserActivityArticleViewDto[0])
        );
    }


    private MongoCollection<Document> getCollection(String collectionName) {
        return mongoClient.getDatabase("monew").getCollection(collectionName);
    }


}
