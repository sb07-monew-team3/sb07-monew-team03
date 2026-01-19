package com.example.monew.domain.comment.service;

import com.example.monew.domain.comment.dto.CommentCursorListRequest;
import com.example.monew.domain.comment.dto.CommentCursorPageResponse;
import com.example.monew.domain.comment.dto.CommentResponse;
import com.example.monew.domain.comment.dto.CursorPageResponse;
import com.example.monew.domain.comment.entity.Comment;
import com.example.monew.domain.comment.repository.CommentLikesRepository;
import com.example.monew.domain.comment.repository.CommentRepository;
import com.example.monew.domain.comment.repository.CommentWithLikeCount;
import com.example.monew.domain.comment.support.CommentCursor;
import com.example.monew.domain.comment.support.CommentLikeCountCursor;
import com.example.monew.global.exception.domain.comment.CommentInvalidRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentQueryService {

    private final CommentRepository commentRepository;
    private final CommentLikesRepository commentLikesRepository;

    public CursorPageResponse<CommentResponse> getCommentsByCursor(CommentCursorListRequest req) {
        return getCommentsByCursor(null, req);
    }

    public CursorPageResponse<CommentResponse> getCommentsByCursor(UUID requesterId, CommentCursorListRequest req) {
        validateRequest(req);

        Sort.Direction direction = req.resolvedDirection();
        int limit = req.resolvedLimit();
        int limitPlusOne = limit + 1;

        String orderBy = req.resolvedOrderBy();

        if (orderBy.equals("createdAt")) {
            return getByCreatedAt(requesterId, req, direction, limit, limitPlusOne);
        }

        if (orderBy.equals("likeCount")) {
            return getByLikeCount(requesterId, req, direction, limit, limitPlusOne);
        }

        throw CommentInvalidRequestException.of("unsupported orderBy");
    }

    public CommentCursorPageResponse<CommentResponse> getCommentsForFront(
            UUID requesterId,
            UUID articleId,
            String orderBy,
            Sort.Direction direction,
            String cursor,
            Boolean after,
            Integer limit
    ) {
        CommentCursorListRequest req = new CommentCursorListRequest(articleId, orderBy, direction, cursor, after, limit);

        String resolvedOrderBy = req.resolvedOrderBy();
        Sort.Direction dir = req.resolvedDirection();
        int resolvedLimit = req.resolvedLimit();
        int limitPlusOne = resolvedLimit + 1;

        Instant cursorCreatedAt = null;
        if (cursor != null && !cursor.isBlank()) {
            try {
                cursorCreatedAt = Instant.parse(cursor);
            } catch (Exception e) {
                throw CommentInvalidRequestException.of("invalid cursor");
            }
        }

        if (resolvedOrderBy.equals("createdAt")) {
            List<Comment> fetched = commentRepository.findByArticleIdWithCreatedAtCursorOnly(
                    articleId,
                    cursorCreatedAt,
                    req.resolvedAfter(),
                    limitPlusOne,
                    dir
            );

            boolean hasNext = fetched.size() > resolvedLimit;
            List<Comment> sliced = hasNext ? fetched.subList(0, resolvedLimit) : fetched;

            List<UUID> commentIds = sliced.stream().map(Comment::getId).toList();
            Map<UUID, Long> likeCountMap = commentLikesRepository.countByCommentIds(commentIds);
            Set<UUID> likedIds = commentLikesRepository.findLikedCommentIds(requesterId, commentIds);

            List<CommentResponse> content = sliced.stream()
                    .map(c -> CommentResponse.from(
                            c,
                            likeCountMap.getOrDefault(c.getId(), 0L),
                            likedIds.contains(c.getId())
                    ))
                    .toList();

            String nextCursor = (hasNext && !sliced.isEmpty())
                    ? sliced.get(sliced.size() - 1).getCreatedAt().toString()
                    : null;

            long totalElements = commentRepository.countByArticleId(articleId);

            return CommentCursorPageResponse.of(
                    content,
                    nextCursor,
                    nextCursor,
                    resolvedLimit,
                    totalElements,
                    hasNext
            );
        }

        if (resolvedOrderBy.equals("likeCount")) {
            long totalElements = commentRepository.countByArticleId(articleId);

            int forcedLimit = (int) Math.min(50L, totalElements);
            if (forcedLimit < 1) forcedLimit = 1;

            int forcedLimitPlusOne = forcedLimit + 1;

            List<CommentWithLikeCount> fetched = commentRepository.findByArticleIdOrderByLikeCountWithCreatedAtCursorOnly(
                    articleId,
                    null,
                    true,
                    forcedLimitPlusOne,
                    dir
            );

            List<CommentWithLikeCount> sliced = fetched.size() > forcedLimit ? fetched.subList(0, forcedLimit) : fetched;

            List<UUID> commentIds = sliced.stream().map(it -> it.comment().getId()).toList();
            Set<UUID> likedIds = commentLikesRepository.findLikedCommentIds(requesterId, commentIds);

            List<CommentResponse> content = sliced.stream()
                    .map(it -> CommentResponse.from(
                            it.comment(),
                            it.likeCount(),
                            likedIds.contains(it.comment().getId())
                    ))
                    .toList();

            return CommentCursorPageResponse.of(
                    content,
                    null,
                    null,
                    forcedLimit,
                    totalElements,
                    false
            );
        }

        throw CommentInvalidRequestException.of("orderBy supports only createdAt or likeCount");
    }


    private CursorPageResponse<CommentResponse> getByCreatedAt(UUID requesterId,
                                                               CommentCursorListRequest req,
                                                               Sort.Direction direction,
                                                               int limit,
                                                               int limitPlusOne) {
        Instant cursorCreatedAt = null;
        UUID cursorId = null;

        if (req.cursor() != null && !req.cursor().isBlank()) {
            try {
                CommentCursor decoded = CommentCursor.decode(req.cursor());
                cursorCreatedAt = decoded.createdAt();
                cursorId = decoded.id();
            } catch (Exception e) {
                throw CommentInvalidRequestException.of("invalid cursor");
            }
        }

        List<Comment> fetched = commentRepository.findByArticleIdWithCursor(
                req.articleId(),
                cursorCreatedAt,
                cursorId,
                req.resolvedAfter(),
                limitPlusOne,
                direction
        );

        boolean hasNext = fetched.size() > limit;
        List<Comment> sliced = hasNext ? fetched.subList(0, limit) : fetched;

        List<UUID> commentIds = sliced.stream().map(Comment::getId).toList();
        Map<UUID, Long> likeCountMap = commentLikesRepository.countByCommentIds(commentIds);
        Set<UUID> likedIds = commentLikesRepository.findLikedCommentIds(requesterId, commentIds);

        String nextCursor = null;
        if (hasNext && !sliced.isEmpty()) {
            Comment last = sliced.get(sliced.size() - 1);
            nextCursor = CommentCursor.encode(last.getCreatedAt(), last.getId());
        }

        List<CommentResponse> items = sliced.stream()
                .map(c -> CommentResponse.from(
                        c,
                        likeCountMap.getOrDefault(c.getId(), 0L),
                        likedIds.contains(c.getId())
                ))
                .toList();

        return CursorPageResponse.of(items, nextCursor, hasNext);
    }

    private CursorPageResponse<CommentResponse> getByLikeCount(UUID requesterId,
                                                               CommentCursorListRequest req,
                                                               Sort.Direction direction,
                                                               int limit,
                                                               int limitPlusOne) {
        Long cursorLikeCount = null;
        Instant cursorCreatedAt = null;
        UUID cursorId = null;

        if (req.cursor() != null && !req.cursor().isBlank()) {
            try {
                CommentLikeCountCursor decoded = CommentLikeCountCursor.decode(req.cursor());
                cursorLikeCount = decoded.likeCount();
                cursorCreatedAt = decoded.createdAt();
                cursorId = decoded.id();
            } catch (Exception e) {
                throw CommentInvalidRequestException.of("invalid cursor");
            }
        }

        List<CommentWithLikeCount> fetched = commentRepository.findByArticleIdOrderByLikeCountWithCursor(
                req.articleId(),
                cursorLikeCount,
                cursorCreatedAt,
                cursorId,
                req.resolvedAfter(),
                limitPlusOne,
                direction
        );

        boolean hasNext = fetched.size() > limit;
        List<CommentWithLikeCount> sliced = hasNext ? fetched.subList(0, limit) : fetched;

        List<UUID> commentIds = sliced.stream().map(it -> it.comment().getId()).toList();
        Set<UUID> likedIds = commentLikesRepository.findLikedCommentIds(requesterId, commentIds);

        String nextCursor = null;
        if (hasNext && !sliced.isEmpty()) {
            CommentWithLikeCount last = sliced.get(sliced.size() - 1);
            nextCursor = CommentLikeCountCursor.encode(
                    last.likeCount(),
                    last.comment().getCreatedAt(),
                    last.comment().getId()
            );
        }

        List<CommentResponse> items = sliced.stream()
                .map(it -> CommentResponse.from(
                        it.comment(),
                        it.likeCount(),
                        likedIds.contains(it.comment().getId())
                ))
                .toList();

        return CursorPageResponse.of(items, nextCursor, hasNext);
    }

    private void validateRequest(CommentCursorListRequest req) {
        if (req.articleId() == null) {
            throw CommentInvalidRequestException.of("articleId is required");
        }

        String orderBy = req.resolvedOrderBy();
        if (!orderBy.equals("createdAt") && !orderBy.equals("likeCount")) {
            throw CommentInvalidRequestException.of("orderBy supports only createdAt or likeCount");
        }
    }
}
