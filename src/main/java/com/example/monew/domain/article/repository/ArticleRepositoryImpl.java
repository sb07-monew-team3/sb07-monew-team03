package com.example.monew.domain.article.repository;

import com.example.monew.domain.article.dto.ArticleDto;
import com.example.monew.domain.article.dto.ArticleRequestDto;
import com.example.monew.domain.article.dto.Order;
import com.example.monew.domain.article.dto.Source;
import com.example.monew.domain.article.entity.QArticle;
import com.example.monew.domain.article.entity.QArticleView;
import com.example.monew.domain.comment.entity.QComment;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.domain.Sort.Direction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class ArticleRepositoryImpl implements ArticleRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private final QArticle article = QArticle.article;
    private final QArticleView articleView = QArticleView.articleView;
    private final QComment comment = QComment.comment;

    @Override
    public Slice<ArticleDto> findArticleSlice(ArticleRequestDto request, UUID userId, List<String> keywords, Pageable pageable) {

        Expression<Boolean> viewedByMe = JPAExpressions
                .selectOne()
                .from(articleView)
                .where(
                        articleView.user.id.eq(userId),
                        articleView.article.id.eq(article.id)
                )
                .exists();

        List<ArticleDto> content = queryFactory
                .select(Projections.constructor(ArticleDto.class,
                        article.id,
                        article.source,
                        article.title,
                        article.publishDate,
                        article.summary,
                        comment.article.id.count().coalesce(0L).as("commentCount"),
                        articleView.article.id.count().coalesce(0L).as("viewCount"),
                        viewedByMe
                ))
                .from(article)
                .leftJoin(articleView).on(articleView.article.id.eq(article.id))
                .leftJoin(comment).on(comment.article.id.eq(article.id))
                .where(
                        keywordOrSummaryContains(keywords),
                        sourcesIn(request.sourceIn()),
                        publishDateBetween(request.publishDateFrom(), request.publishDateTo()),
                        request.orderBy().equals("publishDate") ? cursorCondition(request) : null
                )
                .having(!request.orderBy().equals("publishDate") ? cursorCondition(request) : null)
                .groupBy(article.id)
                .orderBy(getOrderSpecifier(request.getOrder(), request.getDirection()))
                .limit(pageable.getPageSize() + 1)
                .fetch();

        boolean hasNext = content.size() > pageable.getPageSize();
        if (hasNext) {
            content = content.subList(0, pageable.getPageSize());
        }

        return new SliceImpl<>(content, pageable, hasNext);
    }

    // 검색어가 title, summary에 하나라도 부분 일치
    private BooleanExpression keywordOrSummaryContains(List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) return null;

        return keywords.stream()
                .map(kw -> article.title.containsIgnoreCase(kw)
                        .or(article.summary.containsIgnoreCase(kw)))
                .reduce(BooleanExpression::or)
                .orElse(null);
    }

    // 출처(4개 뉴스(중복 가능))
    private BooleanExpression sourcesIn(List<Source> sources) {
        if (sources == null || sources.isEmpty()) return null;

        List<String> sourcelist = sources.stream()
                .map(s -> s.getValue())
                .toList();

        return article.source.in(sourcelist);
    }

    // 날짜
    private BooleanExpression publishDateBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null && end == null) {
            return null;
        }

        if (start != null && end != null) {
            return article.publishDate.between(start, end);
        } else if (start != null) {
            return article.publishDate.goe(start); // (G)reater (O)r (E)qual (>=)
        } else {
            return article.publishDate.loe(end); // (L)ess (O)r (E)qual (<=)
        }
    }

    // 정렬(날짜, 댓글수, 조회수), 정렬 방향(정, 역)
    private OrderSpecifier<?> getOrderSpecifier(Order orderBy, Direction direction) {
        if (orderBy == null) orderBy = Order.PUBLISH_DATE;
        if (direction == null) direction = Direction.DESC;

        boolean asc = direction == Direction.ASC;

        return switch (orderBy) {
            case PUBLISH_DATE -> asc ? article.publishDate.asc() : article.publishDate.desc();
            case COMMENT_COUNT -> asc ? comment.article.id.count().asc() : comment.article.id.count().desc();
            case VIEW_COUNT -> asc ? articleView.article.id.count().asc() : articleView.article.id.count().desc();
        };
    }

    private BooleanExpression cursorCondition(ArticleRequestDto request) {
        // if (request.cursor() == null || request.after() == null) return null;
        if (request.cursor() == null) return null;

        switch (request.orderBy()) {
            case "commentCount" -> {
                if (request.getDirection() == Direction.DESC) {
                    return comment.article.id.count()
                            .loe(Long.parseLong(request.cursor()));
                            //.and(article.createdAt.lt(request.after()));
                } else {
                    return comment.article.id.count()
                            .goe(Long.parseLong(request.cursor()));
                            //.and(article.createdAt.gt(request.after()));
                }

            }
            case "viewCount" -> {
                if (request.getDirection() == Direction.DESC) {
                    return articleView.article.id.count()
                            .loe(Long.parseLong(request.cursor()));
                            //.and(article.createdAt.lt(request.after()));
                } else {
                    return articleView.article.id.count()
                            .goe(Long.parseLong(request.cursor()));
                            //.and(article.createdAt.gt(request.after()));
                }
            }
            default -> { // default는 publishDate 기준
                if (request.getDirection() == Direction.DESC) {
                    return article.publishDate
                            .lt(LocalDateTime.parse(request.cursor()));
                            //.and(article.createdAt.lt(request.after()));
                } else {
                    return article.publishDate
                            .gt(LocalDateTime.parse(request.cursor()));
                            //.and(article.createdAt.gt(request.after()));
                }
            }
        }
    }

    public long countArticleSlice(
            ArticleRequestDto request,
            List<String> keywords
    ) {
        return queryFactory
                .select(article.id.countDistinct())
                .from(article)
                .where(
                        keywordOrSummaryContains(keywords),
                        sourcesIn(request.sourceIn()),
                        publishDateBetween(request.publishDateFrom(), request.publishDateTo())
                )
                .fetchOne();
    }
}
