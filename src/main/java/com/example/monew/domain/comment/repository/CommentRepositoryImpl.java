package com.example.monew.domain.comment.repository;

import com.example.monew.domain.comment.entity.Comment;
import com.example.monew.domain.comment.entity.QComment;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;

import java.time.Instant;
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

    @Override
    public List<Comment> findByArticleIdWithCursor(UUID articleId,
                                                   Instant cursorCreatedAt,
                                                   UUID cursorId,
                                                   boolean after,
                                                   int limitPlusOne,
                                                   Sort.Direction direction) {
        QComment comment = QComment.comment;

        BooleanExpression base = comment.article.id.eq(articleId)
                .and(comment.isDeleted.isFalse());

        BooleanExpression cursorCond = (cursorCreatedAt == null || cursorId == null)
                ? null
                : buildCreatedAtCursorCondition(comment, cursorCreatedAt, cursorId, after, direction);

        OrderSpecifier<?>[] orderBy = buildCreatedAtOrder(comment, direction);

        return jpaQueryFactory
                .selectFrom(comment)
                .where(allOf(base, cursorCond))
                .orderBy(orderBy)
                .limit(limitPlusOne)
                .fetch();
    }

    private BooleanExpression allOf(BooleanExpression... expressions) {
        BooleanExpression result = null;
        for (BooleanExpression exp : expressions) {
            if (exp == null) continue;
            result = (result == null) ? exp : result.and(exp);
        }
        return result;
    }

    // createdAt + id 복합 커서 조건
    private BooleanExpression buildCreatedAtCursorCondition(QComment comment,
                                                            Instant cursorCreatedAt,
                                                            UUID cursorId,
                                                            boolean after,
                                                            Sort.Direction direction) {
        boolean desc = direction == Sort.Direction.DESC;

        if (desc) {
            // DESC
            if (after) {
                return comment.createdAt.lt(cursorCreatedAt)
                        .or(comment.createdAt.eq(cursorCreatedAt).and(comment.id.lt(cursorId)));
            }
            return comment.createdAt.gt(cursorCreatedAt)
                    .or(comment.createdAt.eq(cursorCreatedAt).and(comment.id.gt(cursorId)));
        }

        // ASC
        if (after) {
            return comment.createdAt.gt(cursorCreatedAt)
                    .or(comment.createdAt.eq(cursorCreatedAt).and(comment.id.gt(cursorId)));
        }
        return comment.createdAt.lt(cursorCreatedAt)
                .or(comment.createdAt.eq(cursorCreatedAt).and(comment.id.lt(cursorId)));
    }

    private OrderSpecifier<?>[] buildCreatedAtOrder(QComment comment, Sort.Direction direction) {
        Order order = direction == Sort.Direction.DESC ? Order.DESC : Order.ASC;
        return new OrderSpecifier<?>[]{
                new OrderSpecifier<>(order, comment.createdAt),
                new OrderSpecifier<>(order, comment.id)
        };
    }
}
