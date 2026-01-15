package com.example.monew.domain.article.repository;

import com.example.monew.domain.article.dto.ArticleDto;
import com.example.monew.domain.article.dto.ArticleRequestDto;
import com.example.monew.domain.article.dto.Source;
import com.example.monew.domain.article.entity.QArticle;
import com.example.monew.domain.article.entity.QArticleView;
import com.example.monew.domain.comment.entity.QComment;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
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

        // 집계 함수
        Expression<Long> commentCountSubquery =
                JPAExpressions
                        .select(comment.id.count())
                        .from(comment)
                        .where(
                                comment.article.id.eq(article.id)
                                        .and(comment.isDeleted.eq(false))
                        );

        NumberExpression<Long> commentCount =
                Expressions.numberTemplate(
                        Long.class,
                        "({0})",
                        commentCountSubquery
                );

        Expression<Long> viewCountSubquery =
                JPAExpressions
                        .select(articleView.id.count())
                        .from(articleView)
                        .where(articleView.article.id.eq(article.id));

        NumberExpression<Long> viewCount =
                Expressions.numberTemplate(
                        Long.class,
                        "({0})",
                        viewCountSubquery
                );

        // 페이징으로 기사 조회
        List<ArticleDto> content = queryFactory
                .select(Projections.constructor(ArticleDto.class,
                        article.id,
                        article.source,
                        article.sourceUrl,
                        article.title,
                        article.publishDate,
                        article.summary,
                        commentCount.coalesce(0L),
                        viewCount.coalesce(0L),
                        viewedByMe
                ))
                .from(article)
                .where(
                        notDeleted(),
                        keywordOrSummaryContains(keywords),
                        sourcesIn(request.sourceIn()),
                        publishDateBetween(request.publishDateFrom(), request.publishDateTo()),
                        cursorCondition(request, commentCount, viewCount)
                )
                .groupBy(article.id)
                .orderBy(getOrderSpecifier(request, commentCount, viewCount))
                .limit(pageable.getPageSize() + 1)
                .fetch();

        boolean hasNext = content.size() > pageable.getPageSize();
        if (hasNext) {
            content = content.subList(0, pageable.getPageSize());
        }

        return new SliceImpl<>(content, pageable, hasNext);
    }

    private BooleanExpression notDeleted() {
        return article.isDeleted.eq(false);
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
    private OrderSpecifier<?>[] getOrderSpecifier(
            ArticleRequestDto request,
            NumberExpression<Long> commentCount,
            NumberExpression<Long> viewCount
    ) {
        boolean asc = request.getDirection() == Direction.ASC;

        return switch (request.getOrder()) {
            case PUBLISH_DATE -> asc
                    ? new OrderSpecifier[]{
                            article.publishDate.asc(),
                            article.sortTimestamp.asc()
                    }
                    : new OrderSpecifier[]{
                            article.publishDate.desc(),
                            article.sortTimestamp.desc()
                    };
            case COMMENT_COUNT -> asc
                    ? new OrderSpecifier[]{
                            commentCount.asc(),
                            article.sortTimestamp.asc()
                    }
                            : new OrderSpecifier[]{
                            commentCount.desc(),
                            article.sortTimestamp.desc()
                    };
            case VIEW_COUNT -> asc
                    ? new OrderSpecifier[]{
                            viewCount.asc(),
                            article.sortTimestamp.asc()
                    }
                            : new OrderSpecifier[]{
                            viewCount.desc(),
                            article.sortTimestamp.desc()
                    };
        };
    }

    private BooleanExpression cursorCondition(
            ArticleRequestDto request,
            NumberExpression<Long> commentCount,
            NumberExpression<Long> viewCount
    ) {
         if (request.cursor() == null || request.after() == null) return null;

        switch (request.getOrder()) {
            case COMMENT_COUNT -> {
                if (request.getDirection() == Direction.DESC) {
                    return commentCount
                            .lt(Long.parseLong(request.cursor()))
                            .or(
                                    commentCount.eq(Long.parseLong(request.cursor()))
                                            .and(article.sortTimestamp.lt(request.after()))
                            );
                } else {
                    return commentCount
                            .gt(Long.parseLong(request.cursor()))
                            .or(
                                    commentCount.eq(Long.parseLong(request.cursor()))
                                            .and(article.sortTimestamp.gt(request.after()))
                            );
                }
            }
            case VIEW_COUNT -> {
                if (request.getDirection() == Direction.DESC) {
                    return viewCount
                            .lt(Long.parseLong(request.cursor()))
                            .or(
                                    viewCount.eq(Long.parseLong(request.cursor()))
                                            .and(article.sortTimestamp.lt(request.after()))
                            );
                } else {
                    return viewCount
                            .gt(Long.parseLong(request.cursor()))
                            .or(
                                    viewCount.eq(Long.parseLong(request.cursor()))
                                            .and(article.sortTimestamp.gt(request.after()))
                            );
                }
            }
            default -> { // default는 publishDate 기준
                LocalDateTime cursorDate = LocalDateTime.parse(request.cursor());

                if (request.getDirection() == Direction.DESC) {

                    return article.publishDate
                            .lt(cursorDate)
                            .or(
                                    article.publishDate.eq(cursorDate)
                                            .and(article.sortTimestamp.lt(request.after()))
                            );
                } else {
                    return article.publishDate
                            .gt(cursorDate)
                            .or(
                                    article.publishDate.eq(cursorDate)
                                            .and(article.sortTimestamp.gt(request.after()))
                            );
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
                        notDeleted(),
                        keywordOrSummaryContains(keywords),
                        sourcesIn(request.sourceIn()),
                        publishDateBetween(request.publishDateFrom(), request.publishDateTo())
                )
                .fetchOne();
    }
}
