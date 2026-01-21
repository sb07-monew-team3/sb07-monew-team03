package com.example.monew.domain.activity.service;

import com.example.monew.domain.activity.dto.UserActivityArticleViewDto;
import com.example.monew.domain.activity.dto.UserActivityCommentDto;
import com.example.monew.domain.activity.dto.UserActivityCommentLikeDto;
import com.example.monew.domain.activity.dto.UserActivitySubscriptionDto;
import com.example.monew.domain.comment.dto.CommentResponse;
import com.example.monew.domain.interest.dto.InterestDto;
import com.example.monew.domain.user.dto.UserDto;

import java.util.UUID;

public interface MongoDbService {
     void mongoConnection();
     void insertUserActivity(UserDto userDto);
     void insertUserActivityCommentLike(UUID userId, UserActivityCommentLikeDto commentLikeDto);
     void insertUserActivityComment(UUID userId, UserActivityCommentDto commentDto);
     void insertUserActivitySubscription(UUID userId, UserActivitySubscriptionDto subscriptionDto);
     void insertUserActivityArticleView(UUID userId, UserActivityArticleViewDto articleViewDto);
     void updateUserActivity(UUID userId, UserDto userDto);
     void updateUserActivityComment(UUID userId, CommentResponse commentResponse );
     void updateUserActivitySubscription(UUID userId, InterestDto interestDto);
     void updateCommentWhenCommentLike(UUID userId,UUID commentId );
     void updateCommentLikeWhenCommentLike(UUID userId, UUID commentId);
     void updateCommentWhenUnCommentLike(UUID userId,UUID commentId );
     void updateCommentLikeWhenUnCommentLike(UUID userId, UUID commentId);
     void updateArticleViewWhenArticleView(UUID userId, UUID articleId);
     void updateArticleViewWhenArticleComment(UUID userId, UUID articleId);
     void updateArticleViewWhenCommentDelete(UUID userId, UUID articleId);
     void updateCommentLikeWhenCommentDelete(UUID userId, UUID commentId);
     void updateCommentWhenCommentDelete(UUID userId, UUID commentId);
     void getUserActivity(UUID userId);
}
