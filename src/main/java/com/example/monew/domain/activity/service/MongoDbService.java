package com.example.monew.domain.activity.service;

import com.example.monew.domain.activity.dto.*;
import com.example.monew.domain.comment.dto.CommentResponse;
import com.example.monew.domain.interest.dto.InterestDto;
import com.example.monew.domain.user.dto.UserDto;
import org.bson.Document;

import java.util.List;
import java.util.UUID;

public interface MongoDbService {
     void mongoConnection();
     void insertUserActivity(UserDto userDto);
     void insertUserActivityCommentLike(UUID userId, UserActivityCommentLikeDto commentLikeDto);
     void insertUserActivityComment(UUID userId, UserActivityCommentDto commentDto);
     void insertUserActivitySubscription(UUID userId, UserActivitySubscriptionDto subscriptionDto);
     void insertUserActivityArticleView(UUID userId, UserActivityArticleViewDto articleViewDto);
     void updateUserActivity(UUID userId, UserDto userDto);
     void updateUserActivityComment(CommentResponse commentResponse );
     void updateUserActivitySubscription(InterestDto interestDto);
     void updateWhenArticleView(UUID articleId);
     void updateWhenArticleComment(UUID articleId);
     UserActivityDto getUserActivity(UUID userId);
     void updateWhenCommentLike(UUID commentId);
     void updateWhenUnCommentLike(UUID commentId);
     void deleteWhenUnCommentLike(UUID commentId,UUID userId);
     void updateWhenCommentDelete(UUID commentId, UUID articleId, List<UUID> commentLikesIds);
     void deleteWhenUnSubscription(UUID subscriptionId,UUID userId);
}
