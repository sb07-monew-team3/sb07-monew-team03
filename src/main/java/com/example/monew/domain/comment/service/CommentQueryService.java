package com.example.monew.domain.comment.service;

import com.example.monew.domain.comment.dto.CommentCursorListRequest;
import com.example.monew.domain.comment.dto.CommentResponse;
import com.example.monew.domain.comment.dto.CursorPageResponse;
import com.example.monew.domain.comment.entity.Comment;
import com.example.monew.domain.comment.repository.CommentRepository;
import com.example.monew.domain.comment.support.CommentCursor;
import com.example.monew.global.exception.CustomException;
import com.example.monew.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.monew.global.exception.domain.comment.CommentInvalidRequestException;


import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentQueryService {

    private final CommentRepository commentRepository;

    public CursorPageResponse<CommentResponse> getCommentsByCursor(CommentCursorListRequest req) {
        validateRequest(req);

        Sort.Direction direction = req.resolvedDirection();
        int limit = req.resolvedLimit();
        int limitPlusOne = limit + 1;

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

        String nextCursor = null;
        if (hasNext && !sliced.isEmpty()) {
            Comment last = sliced.get(sliced.size() - 1);
            nextCursor = CommentCursor.encode(last.getCreatedAt(), last.getId());
        }

        List<CommentResponse> items = sliced.stream()
                .map(CommentResponse::from)
                .toList();

        return CursorPageResponse.of(items, nextCursor, hasNext);
    }

    private void validateRequest(CommentCursorListRequest req) {
        if (req.articleId() == null) {
            throw CommentInvalidRequestException.of("articleId is required");
        }

        String orderBy = req.resolvedOrderBy();
        if (!orderBy.equals("createdAt")) {
            throw CommentInvalidRequestException.of("orderBy supports only 'createdAt'");
        }
    }
}
