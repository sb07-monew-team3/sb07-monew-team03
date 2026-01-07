package com.example.monew.domain.user.repository;

import com.example.monew.domain.user.entity.QUser;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;
    @Override
    public boolean isEmailExist(String email) {
        return jpaQueryFactory
                .selectOne()
                .from(QUser.user)
                .where(QUser.user.email.eq(email))
                .fetchOne() != null;
    }
}
