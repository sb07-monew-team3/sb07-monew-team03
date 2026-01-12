package com.example.monew.domain.comment.repository;

import com.example.monew.domain.comment.entity.Comment;
import org.springframework.data.domain.Sort;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface CommentRepositoryCustom {

    List<Comment> getCommentsByUserId(UUID userId);
    Long countByArticleId(UUID articleId);

    List<Comment> findByArticleIdWithCursor(
            UUID articleId,
            Instant cursorCreatedAt,
            UUID cursorId,
            boolean after,
            int limitPlusOne,
            Sort.Direction direction
    );
}
