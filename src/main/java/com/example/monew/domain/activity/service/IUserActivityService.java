package com.example.monew.domain.activity.service;

import com.example.monew.domain.activity.dto.*;
import com.example.monew.domain.activity.mapper.UserActivityArticleViewMapper;
import com.example.monew.domain.activity.mapper.UserActivityCommentLikeMapper;
import com.example.monew.domain.activity.mapper.UserActivityCommentMapper;
import com.example.monew.domain.activity.mapper.UserActivitySubscriptionMapper;
import com.example.monew.domain.article.entity.Article;
import com.example.monew.domain.article.entity.ArticleView;
import com.example.monew.domain.article.repository.ArticleRepository;
import com.example.monew.domain.article.repository.ArticleViewRepository;
import com.example.monew.domain.comment.entity.Comment;
import com.example.monew.domain.comment.entity.CommentLikes;
import com.example.monew.domain.comment.repository.CommentLikesRepository;
import com.example.monew.domain.comment.repository.CommentRepository;
import com.example.monew.domain.interest.entity.Interest;
import com.example.monew.domain.interest.entity.Subscription;
import com.example.monew.domain.interest.repository.InterestRepository;
import com.example.monew.domain.interest.repository.SubscriptionRepository;
import com.example.monew.domain.user.entity.User;
import com.example.monew.domain.user.repository.UserRepository;
import com.example.monew.global.exception.domain.user.UserNotExistException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IUserActivityService implements UserActivityService{

    private final UserActivityArticleViewMapper articleViewMapper;
    private final UserActivityCommentLikeMapper commentLikeMapper;
    private final UserActivityCommentMapper commentMapper;
    private final UserActivitySubscriptionMapper subscriptionMapper;

    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final SubscriptionRepository subscriptionRepostiory;
    private final CommentLikesRepository commentLikesRepository;
    private final ArticleViewRepository articleViewRepository;


    @Override
    @Transactional
    public UserActivityDto getUserActivity(UUID userId) {

        User user = userRepository.findById(userId).orElseThrow(()-> new UserNotExistException(userId));
        List<Subscription> subscriptions = subscriptionRepostiory.getSubscriptionByUserId(userId);
        List<ArticleView> articleViews = articleViewRepository.findAllByUserId(userId);
        List<CommentLikes> commentLikes = commentLikesRepository.getCommentLikesByUserId(userId);
        List<Comment> postedComment = commentRepository.getCommentsByUserId(userId);

        UserActivitySubscriptionDto[] subscriptionDtos = subscriptions.stream()
                .map(subscriptionMapper::toDto)
                .toArray(UserActivitySubscriptionDto[]::new);
        UserActivityArticleViewDto[] articleViewDtos = articleViews.stream()
                .map(articleViewMapper::toDto)
                .toArray(UserActivityArticleViewDto[]::new);
        UserActivityCommentLikeDto[] commentLikeDtos = commentLikes.stream().map(commentLikeMapper::toDto)
                .toArray(UserActivityCommentLikeDto[]::new);
        UserActivityCommentDto[] commentDtos = postedComment.stream().map(commentMapper::toDto)
                .toArray(UserActivityCommentDto[]::new);

        return new UserActivityDto(
                userId,
                user.getEmail(),
                user.getNickName(),
                user.getCreatedAt(),
                subscriptionDtos,
                commentDtos,
                commentLikeDtos,
                articleViewDtos
        );

    }
}
