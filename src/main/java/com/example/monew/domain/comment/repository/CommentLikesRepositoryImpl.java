package com.example.monew.domain.comment.repository;

import com.example.monew.domain.comment.entity.CommentLikes;
import com.example.monew.domain.comment.entity.QCommentLikes;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class CommentLikesRepositoryImpl implements CommentLikesRepositoryCustom{

    private final JPAQueryFactory jpaQueryFactory;
    @Override
    public List<CommentLikes> getCommentLikesByUserId(UUID userId) {
        QCommentLikes commentLikes = QCommentLikes.commentLikes;
        return jpaQueryFactory
                .selectFrom(commentLikes)
                .where(commentLikes.user.id.eq(userId))
                .fetch();
    }

    @Override
    public Long countByCommentId(UUID commentId) {
        QCommentLikes commentLikes = QCommentLikes.commentLikes;
        return jpaQueryFactory
                .select(commentLikes.count())
                .from(commentLikes)
                .where(commentLikes.comment.id.eq(commentId))
                .fetchOne();
    }
}
