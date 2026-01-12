package com.example.monew.domain.comment.repository;

import com.example.monew.domain.comment.entity.Comment;
import com.example.monew.domain.comment.entity.QComment;
import com.example.monew.domain.comment.entity.QCommentLikes;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

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

    @Override
    public List<CommentWithLikeCount> findByArticleIdOrderByLikeCountWithCursor(UUID articleId,
                                                                                Long cursorLikeCount,
                                                                                Instant cursorCreatedAt,
                                                                                UUID cursorId,
                                                                                boolean after,
                                                                                int limitPlusOne,
                                                                                Sort.Direction direction) {
        QComment c = QComment.comment;
        QCommentLikes cl = QCommentLikes.commentLikes;

        NumberExpression<Long> likeCount = cl.id.count();

        BooleanExpression base = c.article.id.eq(articleId)
                .and(c.isDeleted.isFalse());

        BooleanExpression havingCond = (cursorLikeCount == null || cursorCreatedAt == null || cursorId == null)
                ? null
                : buildLikeCountCursorHaving(likeCount, c, cursorLikeCount, cursorCreatedAt, cursorId, after, direction);

        OrderSpecifier<?>[] orderSpecifiers = buildLikeCountOrder(likeCount, c, direction);

        List<Tuple> rows = jpaQueryFactory
                .select(c.id, c.createdAt, likeCount)
                .from(c)
                .leftJoin(cl).on(cl.comment.id.eq(c.id))
                .where(base)
                .groupBy(c.id, c.createdAt)
                .having(havingCond)
                .orderBy(orderSpecifiers)
                .limit(limitPlusOne)
                .fetch();

        if (rows.isEmpty()) return List.of();

        List<UUID> idsInOrder = new ArrayList<>(rows.size());
        Map<UUID, Long> countMap = new HashMap<>();

        for (Tuple row : rows) {
            UUID id = row.get(c.id);
            Long cnt = row.get(likeCount);
            idsInOrder.add(id);
            countMap.put(id, cnt == null ? 0L : cnt);
        }

        List<Comment> comments = jpaQueryFactory
                .selectFrom(c)
                .where(c.id.in(idsInOrder))
                .fetch();

        Map<UUID, Comment> commentMap = comments.stream()
                .collect(Collectors.toMap(Comment::getId, it -> it));

        List<CommentWithLikeCount> result = new ArrayList<>();
        for (UUID id : idsInOrder) {
            Comment comment = commentMap.get(id);
            if (comment != null) {
                result.add(new CommentWithLikeCount(comment, countMap.getOrDefault(id, 0L)));
            }
        }
        return result;
    }

    @Override
    public Page<CommentWithLikeCount> findByArticleIdOrderByLikeCount(UUID articleId, Pageable pageable, Sort.Direction direction) {
        QComment c = QComment.comment;
        QCommentLikes cl = QCommentLikes.commentLikes;

        BooleanExpression base = c.article.id.eq(articleId).and(c.isDeleted.isFalse());
        NumberExpression<Long> likeCount = cl.id.count();

        Long total = jpaQueryFactory
                .select(c.count())
                .from(c)
                .where(base)
                .fetchOne();

        long totalElements = total == null ? 0L : total;

        if (totalElements == 0L) {
            return Page.empty(pageable);
        }

        if (pageable.getOffset() >= totalElements) {
            return new PageImpl<>(List.of(), pageable, totalElements);
        }

        boolean desc = direction == Sort.Direction.DESC;

        List<Tuple> rows = jpaQueryFactory
                .select(c.id, likeCount)
                .from(c)
                .leftJoin(cl).on(cl.comment.id.eq(c.id))
                .where(base)
                .groupBy(c.id, c.createdAt)
                .orderBy(
                        desc ? likeCount.desc() : likeCount.asc(),
                        desc ? c.createdAt.desc() : c.createdAt.asc(),
                        desc ? c.id.desc() : c.id.asc()
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        if (rows.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, totalElements);
        }

        List<UUID> idsInOrder = new ArrayList<>(rows.size());
        Map<UUID, Long> countMap = new HashMap<>();

        for (Tuple row : rows) {
            UUID id = row.get(c.id);
            Long cnt = row.get(likeCount);
            idsInOrder.add(id);
            countMap.put(id, cnt == null ? 0L : cnt);
        }

        List<Comment> comments = jpaQueryFactory
                .selectFrom(c)
                .where(c.id.in(idsInOrder))
                .fetch();

        Map<UUID, Comment> commentMap = comments.stream()
                .collect(Collectors.toMap(Comment::getId, it -> it));

        List<CommentWithLikeCount> content = new ArrayList<>();
        for (UUID id : idsInOrder) {
            Comment comment = commentMap.get(id);
            if (comment != null) {
                content.add(new CommentWithLikeCount(comment, countMap.getOrDefault(id, 0L)));
            }
        }

        return new PageImpl<>(content, pageable, totalElements);
    }

    private BooleanExpression allOf(BooleanExpression... expressions) {
        BooleanExpression result = null;
        for (BooleanExpression exp : expressions) {
            if (exp == null) continue;
            result = (result == null) ? exp : result.and(exp);
        }
        return result;
    }

    private BooleanExpression buildCreatedAtCursorCondition(QComment comment,
                                                            Instant cursorCreatedAt,
                                                            UUID cursorId,
                                                            boolean after,
                                                            Sort.Direction direction) {
        boolean desc = direction == Sort.Direction.DESC;

        if (desc) {
            if (after) {
                return comment.createdAt.lt(cursorCreatedAt)
                        .or(comment.createdAt.eq(cursorCreatedAt).and(comment.id.lt(cursorId)));
            }
            return comment.createdAt.gt(cursorCreatedAt)
                    .or(comment.createdAt.eq(cursorCreatedAt).and(comment.id.gt(cursorId)));
        }

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

    private BooleanExpression buildLikeCountCursorHaving(NumberExpression<Long> likeCount,
                                                         QComment comment,
                                                         long cursorLikeCount,
                                                         Instant cursorCreatedAt,
                                                         UUID cursorId,
                                                         boolean after,
                                                         Sort.Direction direction) {
        boolean desc = direction == Sort.Direction.DESC;

        if (desc) {
            if (after) {
                return likeCount.lt(cursorLikeCount)
                        .or(likeCount.eq(cursorLikeCount).and(comment.createdAt.lt(cursorCreatedAt)))
                        .or(likeCount.eq(cursorLikeCount).and(comment.createdAt.eq(cursorCreatedAt)).and(comment.id.lt(cursorId)));
            }
            return likeCount.gt(cursorLikeCount)
                    .or(likeCount.eq(cursorLikeCount).and(comment.createdAt.gt(cursorCreatedAt)))
                    .or(likeCount.eq(cursorLikeCount).and(comment.createdAt.eq(cursorCreatedAt)).and(comment.id.gt(cursorId)));
        }

        if (after) {
            return likeCount.gt(cursorLikeCount)
                    .or(likeCount.eq(cursorLikeCount).and(comment.createdAt.gt(cursorCreatedAt)))
                    .or(likeCount.eq(cursorLikeCount).and(comment.createdAt.eq(cursorCreatedAt)).and(comment.id.gt(cursorId)));
        }
        return likeCount.lt(cursorLikeCount)
                .or(likeCount.eq(cursorLikeCount).and(comment.createdAt.lt(cursorCreatedAt)))
                .or(likeCount.eq(cursorLikeCount).and(comment.createdAt.eq(cursorCreatedAt)).and(comment.id.lt(cursorId)));
    }

    private OrderSpecifier<?>[] buildLikeCountOrder(NumberExpression<Long> likeCount, QComment comment, Sort.Direction direction) {
        Order order = direction == Sort.Direction.DESC ? Order.DESC : Order.ASC;
        return new OrderSpecifier<?>[]{
                new OrderSpecifier<>(order, likeCount),
                new OrderSpecifier<>(order, comment.createdAt),
                new OrderSpecifier<>(order, comment.id)
        };
    }
}
