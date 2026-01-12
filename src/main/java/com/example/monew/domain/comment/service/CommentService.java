package com.example.monew.domain.comment.service;

import com.example.monew.domain.article.entity.Article;
import com.example.monew.domain.comment.dto.CommentResponse;
import com.example.monew.domain.comment.entity.Comment;
import com.example.monew.domain.comment.repository.CommentLikesRepository;
import com.example.monew.domain.comment.repository.CommentRepository;
import com.example.monew.domain.comment.repository.CommentWithLikeCount;
import com.example.monew.domain.user.entity.User;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;


import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentLikesRepository commentLikesRepository;
    private final EntityManager entityManager;

    public CommentResponse create(UUID userId, UUID articleId, String content) {
        String normalized = normalizeContent(content);

        User user = findUserOrThrow(userId);
        Article article = findArticleOrThrow(articleId);

        Comment saved = commentRepository.save(new Comment(user, article, normalized, false));

        return toResponse(saved, userId);
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

        return new CommentResponse(
                comment.getId(),
                comment.getArticle().getId(),
                comment.getUser().getId(),
                comment.getContent(),
                comment.getCreatedAt(),
                likeCount,
                likedByMe
        );
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
