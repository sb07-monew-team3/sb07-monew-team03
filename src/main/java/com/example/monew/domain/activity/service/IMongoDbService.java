package com.example.monew.domain.activity.service;

import com.example.monew.domain.activity.dto.UserActivityArticleViewDto;
import com.example.monew.domain.activity.dto.UserActivityCommentDto;
import com.example.monew.domain.activity.dto.UserActivityCommentLikeDto;
import com.example.monew.domain.activity.dto.UserActivitySubscriptionDto;
import com.example.monew.domain.comment.dto.CommentResponse;
import com.example.monew.domain.interest.dto.InterestDto;
import com.example.monew.domain.user.dto.UserDto;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class IMongoDbService implements MongoDbService{

    private final MongoClient mongoClient;

    @Override
    public void mongoConnection() {
        MongoDatabase database = mongoClient.getDatabase("admin");
        database.runCommand(new Document("ping", 1));
        System.out.println("Connected to MongoDB!");

    }

    @Override
    public void insertUserActivity(UserDto userDto) {
        MongoCollection<Document> collection = getCollection("userActivity");
        Document doc = new Document()
                .append("id", userDto.id().toString())
                .append("email", userDto.email())
                .append("nickname", userDto.nickname())
                .append("createdAt", userDto.createdAt().toString())
                .append("subscriptions", new ArrayList<>())
                .append("comments", new ArrayList<>())
                .append("commentsLikes", new ArrayList<>())
                .append("articleViews", new ArrayList<>());

        collection.insertOne(doc);
    }

    @Override
    public void insertUserActivityCommentLike(UUID userId, UserActivityCommentLikeDto commentLikeDto) {
        MongoCollection<Document> collection = getCollection("userActivity");
        Document commentLike = new Document()
                .append("id", commentLikeDto.id())
                .append("createdAt", commentLikeDto.createdAt().toString())
                .append("commentId", commentLikeDto.commentId())
                .append("articleId", commentLikeDto.articleId())
                .append("articleTitle", commentLikeDto.articleTitle())
                .append("commentUserId", commentLikeDto.commentUserId())
                .append("commentUserNickname", commentLikeDto.commentUserNickname())
                .append("commentContent", commentLikeDto.commentContent())
                .append("commentLikeCount", commentLikeDto.commentLikeCount())
                .append("commentCreatedAt", commentLikeDto.commentCreatedAt().toString());
        collection.updateOne(
                Filters.eq("id", userId.toString()),
                Updates.push("commentsLikes", commentLike)
                );
    }

    @Override
    public void insertUserActivityComment(UUID userId, UserActivityCommentDto commentDto) {

        MongoCollection<Document> collection = getCollection("userActivity");
        Document comment = new Document()
                .append("id", commentDto.id())
                .append("articleId", commentDto.articleId())
                .append("articleTitle", commentDto.articleTitle())
                .append("userId", commentDto.userId())
                .append("userNickname", commentDto.userNickname())
                .append("content", commentDto.content())
                .append("likeCount", commentDto.likeCount())
                .append("createdAt", commentDto.createdAt().toString());
        collection.updateOne(
                Filters.eq("id", userId.toString()),
                Updates.push("comments", comment)
        );
    }

    @Override
    public void insertUserActivitySubscription(UUID userId, UserActivitySubscriptionDto subscriptionDto) {

        MongoCollection<Document> collection = getCollection("userActivity");
        Document subscription = new Document()
                .append("id", subscriptionDto.id())
                .append("interestId", subscriptionDto.interestId())
                .append("interestName", subscriptionDto.interestName())
                .append("interestKeywords", subscriptionDto.interestKeywords())
                .append("interestSubscriberCount", subscriptionDto.interestSubscriberCount())
                .append("createdAt", subscriptionDto.createdAt().toString());
        collection.updateOne(
                Filters.eq("id", userId.toString()),
                Updates.push("subscriptions", subscription)
        );
    }

    @Override
    public void insertUserActivityArticleView(UUID userId, UserActivityArticleViewDto articleViewDto) {
        MongoCollection<Document> collection = getCollection("userActivity");
        Document articleView = new Document()
                .append("id", articleViewDto.id())
                .append("viewedBy", articleViewDto.viewedBy())
                .append("createdAt", articleViewDto.createdAt().toString())
                .append("articleId", articleViewDto.articleId())
                .append("source", articleViewDto.source())
                .append("sourceUrl", articleViewDto.sourceUrl())
                .append("articleTitle", articleViewDto.articleTitle())
                .append("articlePublishedDate", articleViewDto.articlePublishedDate().toString())
                .append("articleSummary", articleViewDto.articleSummary())
                .append("articleCommentCount", articleViewDto.articleCommentCount())
                .append("articleViewCount", articleViewDto.articleViewCount());
        collection.updateOne(
                Filters.eq("id", userId.toString()),
                Updates.push("articleViews", articleView)
        );
    }

    @Override
    public void updateUserActivity(UUID userId, UserDto userDto) {
        MongoCollection<Document> collection = getCollection("userActivity");
        collection.updateOne(
                Filters.eq("id", userId.toString()),
                Updates.set("nickname", userDto.nickname())
        );
    }

    @Override
    public void updateUserActivityComment(UUID userId, CommentResponse commentResponse ) {
        MongoCollection<Document> collection = getCollection("userActivity");
        collection.updateOne(
                Filters.eq("id", userId.toString()),
                Updates.set("comments.$[element].content", commentResponse.getContent()),
                new UpdateOptions().arrayFilters(
                        List.of(Filters.eq("element.id", commentResponse.getId()))
                )
        );
    }

    @Override
    public void updateUserActivitySubscription(UUID userId, InterestDto interestDto) {
        MongoCollection<Document> collection = getCollection("userActivity");
        collection.updateOne(
                Filters.eq("id", userId.toString()),
                Updates.set("subscriptions.$[element].interestKeywords", interestDto.keywords())
                , new UpdateOptions().arrayFilters(
                        List.of(Filters.eq("element.interestId", interestDto.id()))
                )
        );
    }

    @Override
    public void updateCommentWhenCommentLike(UUID userId,UUID commentId ){
        MongoCollection<Document> collection = getCollection("userActivity");
        collection.updateOne(
                Filters.eq("id", userId.toString()),
                Updates.inc("comments.$[element].likeCount", 1)
                , new UpdateOptions().arrayFilters(
                        List.of(Filters.eq("element.id", commentId))
                )
        );
    }

    @Override
    public void updateCommentLikeWhenCommentLike(UUID userId, UUID commentId){
        MongoCollection<Document> collection = getCollection("userActivity");
        collection.updateOne(
                Filters.eq("id", userId.toString()),
                Updates.inc("commentsLikes.$[element].commentLikeCount", 1)
                , new UpdateOptions().arrayFilters(
                        List.of(Filters.eq("element.commentId", commentId))
                )
        );
    }

    @Override
    public void updateCommentWhenUnCommentLike(UUID userId,UUID commentId ){
        MongoCollection<Document> collection = getCollection("userActivity");
        collection.updateOne(
                Filters.eq("id", userId.toString()),
                Updates.inc("comments.$[element].likeCount", -1)
                , new UpdateOptions().arrayFilters(
                        List.of(Filters.eq("element.id", commentId))
                )
        );
    }

    @Override
    public void updateCommentLikeWhenUnCommentLike(UUID userId, UUID commentId){
        MongoCollection<Document> collection = getCollection("userActivity");
        collection.updateOne(
                Filters.eq("id", userId.toString()),
                Updates.inc("commentsLikes.$[element].commentLikeCount", -1)
                , new UpdateOptions().arrayFilters(
                        List.of(Filters.eq("element.commentId", commentId))
                )
        );
    }

    @Override
    public void updateArticleViewWhenArticleView(UUID userId, UUID articleId){
        MongoCollection<Document> collection = getCollection("userActivity");
        collection.updateOne(
                Filters.eq("id", userId.toString()),
                Updates.inc("articleViews.$[element].articleViewCount", 1),
                new UpdateOptions().arrayFilters(
                        List.of(Filters.eq("element.articleId", articleId))
                )
        );
    }

    @Override
    public void updateArticleViewWhenArticleComment(UUID userId, UUID articleId){
        MongoCollection<Document> collection = getCollection("userActivity");
        collection.updateOne(
                Filters.eq("id", userId.toString()),
                Updates.inc("articleViews.$[element].articleCommentCount", 1),
                new UpdateOptions().arrayFilters(
                        List.of(Filters.eq("element.articleId", articleId))
                )
        );
    }

    @Override
    public void updateArticleViewWhenCommentDelete(UUID userId, UUID articleId){
        MongoCollection<Document> collection = getCollection("userActivity");
        collection.updateOne(
                Filters.eq("id", userId.toString()),
                Updates.inc("articleViews.$[element].articleCommentCount", -1),
                new UpdateOptions().arrayFilters(
                        List.of(Filters.eq("element.articleId", articleId))
                )
        );
    }

    @Override
    public void updateCommentLikeWhenCommentDelete(UUID userId, UUID commentId){
        MongoCollection<Document> collection = getCollection("userActivity");
        collection.updateOne(
                Filters.eq("id", userId.toString()),
                Updates.pull("commentsLikes", Filters.eq("commentId", commentId))
        );
    }

    @Override
    public void updateCommentWhenCommentDelete(UUID userId, UUID commentId){
        MongoCollection<Document> collection = getCollection("userActivity");
        collection.updateOne(
                Filters.eq("id", userId.toString()),
                Updates.pull("comments", Filters.eq("id", commentId))
        );
    }

    @Override
    public void getUserActivity(UUID userId) {
        MongoCollection<Document> collection = getCollection("userActivity");

    }


    private MongoCollection<Document> getCollection(String collectionName) {
        return mongoClient.getDatabase("monew").getCollection(collectionName);
    }
}
