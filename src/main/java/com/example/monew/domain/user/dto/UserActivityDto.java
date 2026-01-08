package com.example.monew.domain.user.dto;

import com.example.monew.domain.article.entity.ArticleView;
import com.example.monew.domain.comment.entity.Comment;
import com.example.monew.domain.comment.entity.CommentLikes;
import com.example.monew.domain.interest.entity.Subscription;

import java.lang.reflect.Array;
import java.time.Instant;
import java.util.UUID;

public record UserActivityDto(

        UUID id,
        String email,
        String nickname,
        Instant createdAt,
        Subscription[] subscriptions,
        Comment[] comments,
        CommentLikes[] commentLikes,
        ArticleView[] articleViews

) {
}
