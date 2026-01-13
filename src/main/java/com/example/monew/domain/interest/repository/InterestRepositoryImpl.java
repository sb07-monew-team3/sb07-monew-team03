package com.example.monew.domain.interest.repository;

import com.example.monew.domain.interest.entity.Interest;
import com.example.monew.domain.interest.entity.QInterest;
import com.example.monew.domain.interest.entity.QKeyword;
import com.example.monew.domain.interest.entity.QSubscription;
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

        JPAQuery<Interest> query = jpaQueryFactory
                .selectFrom(qInterest)
                .leftJoin(qKeyword)
                .on(qKeyword.interest.id.eq(qInterest.id))
                .leftJoin(qSubscription)
                .on(qSubscription.interest.id.eq(qInterest.id));

        // 부분일치
        if (keyword != null && !keyword.isBlank()) {
            query.where(
                    qInterest.name.containsIgnoreCase(keyword)
                            .or(qKeyword.keyword.containsIgnoreCase(keyword))
            );
        }
        query.groupBy(qInterest.id); // 중복 없이 count 계산?

        //커서
        if (cursor != null && !cursor.isBlank() && after != null) {
            if ("name".equals(orderBy)) {
                if ("ASC".equals(direction)) {
                    query.where(
                            qInterest.name.gt(cursor)
                                    .or(qInterest.name.eq(cursor)
                                            .and(qInterest.createdAt.gt(after)))
                    );
                } else {
                    query.where(
                            qInterest.name.lt(cursor)
                                    .or(qInterest.name.eq(cursor)
                                            .and(qInterest.createdAt.lt(after)))
                    );
                }
            } else if ("subscriberCount".equals(orderBy)) {
                Long cursorCount = Long.parseLong(cursor);
                if ("ASC".equals(direction)) {
                    query.having(
                            qSubscription.id.count().gt(cursorCount)
                                    .or(qSubscription.id.count().eq(cursorCount)
                                            .and(qInterest.createdAt.gt(after)))
                    );
                } else {
                    query.having(
                            qSubscription.id.count().lt(cursorCount)
                                    .or(qSubscription.id.count().eq(cursorCount)
                                            .and(qInterest.createdAt.lt(after)))
                    );
                }
            }
        }

        // 정렬
        if ("name".equals(orderBy)) {
            if ("ASC".equals(direction)) {
                query.orderBy(qInterest.name.asc());
            } else {
                query.orderBy(qInterest.name.desc());
            }
        } else if ("subscriberCount".equals(orderBy)) {
            if ("ASC".equals(direction)) {
                query.orderBy(qSubscription.id.count().asc());
            } else {
                query.orderBy(qSubscription.id.count().desc());
            }
        }

        query.limit(limit + 1);
        return query.fetch();
    }
}
