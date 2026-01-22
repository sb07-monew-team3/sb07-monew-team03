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
     void updateUserActivity(UserDto userDto);
     void updateUserActivityComment(CommentResponse commentResponse );
     void updateWhenUnCommentLike(UUID userId,UUID commentId);
     void updateWhenCommentDelete(UUID articleId);
     void updateSubscription(InterestDto interestDto);
     void updateWhenUnSubscription(UUID subscriptionId,UUID userId);
     void updateWhenSubscriptionDelete(UUID interestId, List<UUID> userIds);
     UserActivityDto getUserActivity(UUID userId);

}
