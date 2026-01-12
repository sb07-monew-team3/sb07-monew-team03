package com.example.monew.domain.comment.repository;

import com.example.monew.domain.comment.entity.CommentLikes;
import com.example.monew.domain.comment.entity.QCommentLikes;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class CommentLikesRepositoryImpl implements CommentLikesRepositoryCustom {

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

    @Override
    public Map<UUID, Long> countByCommentIds(List<UUID> commentIds) {
        if (commentIds == null || commentIds.isEmpty()) return Map.of();

        QCommentLikes cl = QCommentLikes.commentLikes;

        List<Tuple> rows = jpaQueryFactory
                .select(cl.comment.id, cl.id.count())
                .from(cl)
                .where(cl.comment.id.in(commentIds))
                .groupBy(cl.comment.id)
                .fetch();

        Map<UUID, Long> result = new HashMap<>();
        for (Tuple row : rows) {
            UUID id = row.get(cl.comment.id);
            Long cnt = row.get(cl.id.count());
            result.put(id, cnt == null ? 0L : cnt);
        }
        return result;
    }

    @Override
    public Set<UUID> findLikedCommentIds(UUID userId, List<UUID> commentIds) {
        if (userId == null || commentIds == null || commentIds.isEmpty()) return Set.of();

        QCommentLikes cl = QCommentLikes.commentLikes;

        List<UUID> ids = jpaQueryFactory
                .select(cl.comment.id)
                .from(cl)
                .where(cl.user.id.eq(userId).and(cl.comment.id.in(commentIds)))
                .fetch();

        return new HashSet<>(ids);
    }
}
