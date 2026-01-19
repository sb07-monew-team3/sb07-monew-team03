package com.example.monew.domain.comment.service;

import com.example.monew.domain.article.entity.Article;
import com.example.monew.domain.comment.dto.CommentResponse;
import com.example.monew.domain.comment.entity.Comment;
import com.example.monew.domain.comment.repository.CommentLikesRepository;
import com.example.monew.domain.comment.repository.CommentRepository;
import com.example.monew.domain.comment.repository.CommentWithLikeCount;
import com.example.monew.global.exception.domain.comment.CommentInvalidRequestException;
import com.example.monew.domain.user.entity.User;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 50;
    private static final Set<String> ALLOWED_ORDER_BY = Set.of("createdAt", "likeCount");

    private final CommentRepository commentRepository;
    private final CommentLikesRepository commentLikesRepository;
    private final EntityManager entityManager;

    @Transactional(readOnly = true)
    public Page<CommentResponse> list(
            UUID userId,
            UUID articleId,
            String orderBy,
            Sort.Direction direction,
            Integer page,
            Integer limit
    ) {
        String resolvedOrderBy = resolveOrderBy(orderBy);
        Sort.Direction resolvedDirection = resolveDirection(direction);
        int resolvedPage = resolvePage(page);
        int resolvedLimit = resolveLimit(limit);

        Pageable pageable = PageRequest.of(
                resolvedPage,
                resolvedLimit,
                Sort.by(resolvedDirection, resolvedOrderBy)
        );

        return list(userId, articleId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<CommentResponse> list(UUID userId, UUID articleId, Pageable pageable) {
        findArticleOrThrow(articleId);

        Sort.Order likeCountOrder = pageable.getSort().getOrderFor("likeCount");
        if (likeCountOrder != null) {
            Sort.Direction dir = likeCountOrder.getDirection() == null ? Sort.Direction.DESC : likeCountOrder.getDirection();

            Page<CommentWithLikeCount> page = commentRepository.findByArticleIdOrderByLikeCount(articleId, pageable, dir);

            List<UUID> commentIds = page.getContent().stream().map(it -> it.comment().getId()).toList();
            Set<UUID> likedIds = commentLikesRepository.findLikedCommentIds(userId, commentIds);

            return page.map(it -> CommentResponse.from(
                    it.comment(),
                    it.likeCount(),
                    likedIds.contains(it.comment().getId())
            ));
        }

        Page<Comment> page = commentRepository.findByArticle_IdAndIsDeletedFalse(articleId, pageable);

        List<UUID> commentIds = page.getContent().stream().map(Comment::getId).toList();
        Map<UUID, Long> likeCountMap = commentLikesRepository.countByCommentIds(commentIds);
        Set<UUID> likedIds = commentLikesRepository.findLikedCommentIds(userId, commentIds);

        return page.map(c -> CommentResponse.from(
                c,
                likeCountMap.getOrDefault(c.getId(), 0L),
                likedIds.contains(c.getId())
        ));
    }

    private String resolveOrderBy(String orderBy) {
        String resolved = (orderBy == null || orderBy.isBlank()) ? "createdAt" : orderBy.trim();
        if (!ALLOWED_ORDER_BY.contains(resolved)) {
            throw CommentInvalidRequestException.of("orderBy supports only " + ALLOWED_ORDER_BY);
        }
        return resolved;
    }

    private Sort.Direction resolveDirection(Sort.Direction direction) {
        return direction == null ? Sort.Direction.DESC : direction;
    }

    private int resolvePage(Integer page) {
        if (page == null) return 0;
        return Math.max(page, 0);
    }

    private int resolveLimit(Integer limit) {
        if (limit == null) return DEFAULT_LIMIT;
        if (limit < 1) return 1;
        return Math.min(limit, MAX_LIMIT);
    }

    public CommentResponse create(UUID userId, UUID articleId, String content) {
        String normalized = normalizeContent(content);

        User user = findUserOrThrow(userId);
        Article article = findArticleOrThrow(articleId);

        Comment saved = commentRepository.save(new Comment(user, article, normalized, false));

        return toResponse(saved, userId);
    }

    public CommentResponse update(UUID userId, UUID commentId, String content) {
        String normalized = normalizeContent(content);

        Comment comment = commentRepository.findByIdAndIsDeletedFalse(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));

        if (!comment.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed");
        }

        comment.updateContent(normalized);

        return toResponse(comment, userId);
    }

    public void softDelete(UUID userId, UUID commentId) {
        Comment comment = commentRepository.findByIdAndIsDeletedFalse(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));

        if (userId != null && !comment.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed");
        }

        comment.delete();
    }

    public void hardDelete(UUID userId, UUID commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));

        if (userId != null && !comment.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed");
        }

        commentRepository.delete(comment);
    }

    private CommentResponse toResponse(Comment comment, UUID requesterUserId) {
        long likeCount = commentLikesRepository.countByComment_Id(comment.getId());
        boolean likedByMe = commentLikesRepository.existsByUserIdAndCommentId(requesterUserId, comment.getId());

        return CommentResponse.from(comment, likeCount, likedByMe);
    }

    private User findUserOrThrow(UUID userId) {
        User user = entityManager.find(User.class, userId);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        return user;
    }

    private Article findArticleOrThrow(UUID articleId) {
        Article article = entityManager.find(Article.class, articleId);
        if (article == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Article not found");
        }
        return article;
    }

    private String normalizeContent(String content) {
        if (content == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Content is required");
        }
        String trimmed = content.trim();
        if (trimmed.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Content is required");
        }
        return trimmed;
    }
}
