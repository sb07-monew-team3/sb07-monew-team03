package com.example.monew.domain.comment.repository;

import com.example.monew.domain.comment.entity.Comment;
import com.example.monew.domain.comment.entity.QComment;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class CommentRepositoryImpl implements CommentRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<Comment> getCommentsByUserId(UUID userId) {
        QComment comment = QComment.comment;
        return jpaQueryFactory
                .selectFrom(comment)
                .where(comment.user.id.eq(userId))
                .fetch();
    }
    @Override
    public Long countByArticleId(UUID articleId) {
        QComment comment = QComment.comment;
        return jpaQueryFactory
                .select(comment.count())
                .from(comment)
                .where(comment.article.id.eq(articleId))
                .fetchOne();
    }
}
