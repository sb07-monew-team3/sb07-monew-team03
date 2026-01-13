package com.example.monew.domain.interest.repository;

import com.example.monew.domain.interest.entity.QSubscription;
import com.example.monew.domain.interest.entity.Subscription;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class SubscriptionRepositoryImpl implements SubscriptionRepositoryCustom{

    private final JPAQueryFactory jpaQueryFactory;


    @Override
    public List<Subscription> getSubscriptionByUserId(UUID userId) {
        QSubscription qSubscription = QSubscription.subscription;
        return jpaQueryFactory
                .selectFrom(qSubscription)
                .where(qSubscription.user.id.eq(userId))
                .fetch();
    }

    @Override
    public Long countByInterestId(UUID interestId) {
        QSubscription subscription = QSubscription.subscription;
        return jpaQueryFactory
                .select(subscription.count())
                .from(subscription)
                .where(subscription.interest.id.eq(interestId))
                .fetchOne();
    }

    @Override
    public List<Subscription> getSubscriptionsByInterestId(UUID interestId) {
        QSubscription subscription = QSubscription.subscription;
        return jpaQueryFactory
                .selectFrom(subscription)
                .where(subscription.interest.id.eq(interestId))
                .fetch();
    }
}
