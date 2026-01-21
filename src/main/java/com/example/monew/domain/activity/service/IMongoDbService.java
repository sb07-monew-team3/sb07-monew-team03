package com.example.monew.domain.activity.service;

import com.example.monew.domain.activity.dto.*;
import com.example.monew.domain.activity.mapper.UserActivityMapper;
import com.example.monew.domain.comment.dto.CommentResponse;
import com.example.monew.domain.interest.dto.InterestDto;
import com.example.monew.domain.user.dto.UserDto;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class IMongoDbService implements MongoDbService{

    private final MongoClient mongoClient;
    private final UserActivityMapper userActivityMapper;

    @Override
    public void mongoConnection() {
                MongoDatabase database = mongoClient.getDatabase("admin");
                database.runCommand(new Document("ping", 1));
               log.info("MongoDb Connection Success");
    }



    @Override
    public void insertUserActivity(UserDto userDto) {
        MongoCollection<Document> collection = getCollection("userActivity");
        Document doc = new Document()
                .append("id", userDto.id().toString())
                .append("email", userDto.email())
                .append("nickname", userDto.nickname())
                .append("createdAt", userDto.createdAt().toString())
                .append("subscriptions", new Document())
                .append("comments", new Document())
                .append("commentsLikes", new Document())
                .append("articleViews", new Document());

        collection.insertOne(doc);
    }

    @Override
    public void insertUserActivityCommentLike(UUID userId, UserActivityCommentLikeDto commentLikeDto) {
        MongoCollection<Document> collection = getCollection("userActivity");
        String commentLikeKey = commentLikeDto.id().toString();
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
                Filters.eq("id", userId.toString()),
                Updates.set("commentsLikes." + commentLikeKey, commentLike)
                );
    }

    @Override
    public void insertUserActivityComment(UUID userId, UserActivityCommentDto commentDto) {

        MongoCollection<Document> collection = getCollection("userActivity");
        String commentKey= commentDto.id().toString();
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
                Filters.eq("id", userId.toString()),
                Updates.set("comments." + commentKey, comment)
        );
    }

    @Override
    public void insertUserActivitySubscription(UUID userId, UserActivitySubscriptionDto subscriptionDto) {

        MongoCollection<Document> collection = getCollection("userActivity");
        String subscriptionKey = subscriptionDto.id().toString();
        Document subscription = new Document()
                .append("interestId", subscriptionDto.interestId().toString())
                .append("interestName", subscriptionDto.interestName())
                .append("interestKeywords", Arrays.asList(subscriptionDto.interestKeywords()))
                .append("interestSubscriberCount", subscriptionDto.interestSubscriberCount())
                .append("createdAt", subscriptionDto.createdAt().toString());
        collection.updateOne(
                Filters.eq("id", userId.toString()),
                Updates.set("subscriptions." + subscriptionKey, subscription)
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
                .append("articlePublishedDate", articleViewDto.articlePublishedDate().toString())
                .append("articleSummary", articleViewDto.articleSummary())
                .append("articleCommentCount", articleViewDto.articleCommentCount())
                .append("articleViewCount", articleViewDto.articleViewCount());
        collection.updateOne(
                Filters.eq("id", userId.toString()),
                Updates.set("articleViews." + articleKey, articleView)
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
    public void updateUserActivityComment(CommentResponse commentResponse ) {
        MongoCollection<Document> collection = getCollection("userActivity");
        collection.updateOne(
                Filters.exists("comments."+ commentResponse.getId().toString()),
                Updates.set("comments."+ commentResponse.getId()+".content", commentResponse.getContent())
        );
    }

    @Override
    public void updateUserActivitySubscription(InterestDto interestDto) {
        MongoCollection<Document> collection = getCollection("userActivity");
        collection.updateMany(
                Filters.exists("subscriptions." + interestDto.id().toString()),
                Updates.set("subscriptions." + interestDto.id().toString()+".interestKeywords", Arrays.asList(interestDto.keywords()))
        );
    }

    @Override
    public void updateWhenArticleView(UUID articleId){
        MongoCollection<Document> collection = getCollection("userActivity");
        collection.updateMany(
                Filters.exists("articleViews."+ articleId.toString()),
                Updates.inc("articleViews."+ articleId+".articleViewCount", 1)

        );
    }

    @Override
    public void updateWhenArticleComment(UUID articleId){
        MongoCollection<Document> collection = getCollection("userActivity");
        collection.updateOne(
                Filters.exists("articleViews."+ articleId.toString()),
                Updates.inc("articleViews." + articleId+".articleCommentCount", 1)
        );
    }

    @Override
    public UserActivityDto getUserActivity(UUID userId) {
        MongoCollection<Document> collection = getCollection("userActivity");
        Document document = collection.find(Filters.eq("id", userId.toString()))
                .first();

        return userActivityMapper.toUserActivityDto(document);
    }

    @Override
    public void updateWhenCommentLike(UUID commentId,List<UUID> commentLikesIds){
        MongoCollection<Document> collection = getCollection("userActivity");
        collection.updateMany(
                Filters.exists("comments."+ commentId.toString()),
                Updates.inc("comments."+ commentId.toString() +".likeCount", 1)
        );
        commentLikesIds.stream().forEach(x->
                collection.updateOne(

                        Filters.exists("commentsLikes."+ x.toString()),
                        Updates.inc("commentsLikes."+ x.toString()+".commentLikeCount", 1)
                )
                );


    }

    @Override
    public void updateWhenUnCommentLike(UUID commentId,List<UUID> commentLikesIds){
        MongoCollection<Document> collection = getCollection("userActivity");
        collection.updateMany(
                Filters.exists("comments."+ commentId.toString()),
                Updates.inc("comments." + commentId+".likeCount", -1)
        );
        commentLikesIds.stream().forEach(x->
                collection.updateOne(

                        Filters.exists("commentsLikes."+ x.toString()),
                        Updates.inc("commentsLikes."+ x.toString()+".commentLikeCount", -1)
                )
        );
    }

    @Override
    public void deleteWhenUnCommentLike(UUID commentLikeId,UUID userId){
        MongoCollection<Document> collection = getCollection("userActivity");
        collection.updateOne(
                Filters.eq("id", userId.toString()),
                Updates.unset("commentsLikes."+ commentLikeId.toString())
        );
    }


    @Override
    public void updateWhenCommentDelete(UUID commentId,UUID articleId,List<UUID> commentLikesIds){

        MongoCollection<Document> collection = getCollection("userActivity");
        collection.updateMany(
                Filters.exists("articleViews."+ articleId.toString()),
                Updates.inc("articleViews."+ articleId.toString()+".articleCommentCount", -1)
        );
    }

    @Override
    public void deleteWhenUnSubscription(UUID subscriptionId,UUID userId) {
        MongoCollection<Document> collection = getCollection("userActivity");
        collection.updateOne(
                Filters.eq("id",userId.toString()),
                Updates.unset("subscriptions."+ subscriptionId.toString())
        );
    }

    private MongoCollection<Document> getCollection(String collectionName) {
        return mongoClient.getDatabase("monew").getCollection(collectionName);
    }


}
