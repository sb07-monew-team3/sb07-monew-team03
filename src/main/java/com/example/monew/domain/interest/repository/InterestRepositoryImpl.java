package com.example.monew.domain.interest.repository;

import com.example.monew.domain.interest.entity.Interest;
import com.example.monew.domain.interest.entity.QInterest;
import com.example.monew.domain.interest.entity.QKeyword;
import com.example.monew.domain.interest.entity.QSubscription;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.List;

@RequiredArgsConstructor
public class InterestRepositoryImpl implements InterestRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<Interest> searchByInterestOrKeyword(
            String keyword,
            String orderBy,
            String direction,
            String cursor,
            Instant after,
            int limit) {

        QInterest qInterest = QInterest.interest;
        QKeyword qKeyword = QKeyword.keyword1;
        QSubscription qSubscription = QSubscription.subscription;

        return jpaQueryFactory
                .selectFrom(qInterest)
                .leftJoin(qKeyword).on(qKeyword.interest.id.eq(qInterest.id))
                .leftJoin(qSubscription).on(qSubscription.interest.id.eq(qInterest.id))
                .where(
                        containsKeyword(keyword),
                        nameCursor(orderBy, direction, cursor, after))
                .groupBy(qInterest.id)
                .having(subscriberCountCursor(orderBy, direction, cursor, after))
                .orderBy(orderCondition(orderBy, direction))
                .limit(limit + 1)
                .fetch();

    }

    // 부분 일치
    private BooleanExpression containsKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) return null;

        return QInterest.interest.name.containsIgnoreCase(keyword)
                .or(QKeyword.keyword1.keyword.containsIgnoreCase(keyword));
    }

    // 이름 커서
    private BooleanExpression nameCursor(String orderBy, String direction, String cursor, Instant after) {
        if (!"name".equals(orderBy) || cursor == null || after == null) return null;

        QInterest qInterest = QInterest.interest;

        return "ASC".equalsIgnoreCase(direction)
                ? qInterest.name.gt(cursor).or(qInterest.name.eq(cursor).and(qInterest.createdAt.gt(after)))
                : qInterest.name.lt(cursor).or(qInterest.name.eq(cursor).and(qInterest.createdAt.lt(after)));
    }

    // 구독자수 커서
    private BooleanExpression subscriberCountCursor(String orderBy, String direction, String cursor, Instant after) {
        if (!"subscriberCount".equals(orderBy) || cursor == null || after == null) return null;

        long cursorCount = Long.parseLong(cursor);
        QInterest qInterest = QInterest.interest;
        QSubscription qSubscription = QSubscription.subscription;

        return "ASC".equalsIgnoreCase(direction)
                ? qSubscription.id.countDistinct().gt(cursorCount)
                .or(qSubscription.id.countDistinct().eq(cursorCount).and(qInterest.createdAt.gt(after)))
                : qSubscription.id.countDistinct().lt(cursorCount)
                .or(qSubscription.id.countDistinct().eq(cursorCount).and(qInterest.createdAt.lt(after)));
    }

    // 정렬
    private OrderSpecifier<?>[] orderCondition(String orderBy, String direction) {

        QInterest qInterest = QInterest.interest;
        QSubscription qSubscription = QSubscription.subscription;
        boolean isAsc = "ASC".equalsIgnoreCase(direction);

        if ("name".equals(orderBy)) {
            return isAsc
                    ? new OrderSpecifier[]{qInterest.name.asc(), qInterest.createdAt.asc()}
                    : new OrderSpecifier[]{qInterest.name.desc(), qInterest.createdAt.desc()};
        } else if ("subscriberCount".equals(orderBy)) {
            return isAsc
                    ? new OrderSpecifier[]{qSubscription.id.countDistinct().asc(), qInterest.createdAt.asc()}
                    : new OrderSpecifier[]{qSubscription.id.countDistinct().desc(), qInterest.createdAt.desc()};
        }

        return new OrderSpecifier[]{qInterest.createdAt.desc()};
    }
}
