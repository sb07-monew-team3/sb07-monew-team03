package com.example.monew.domain.comment.service;

import com.example.monew.domain.comment.entity.Comment;
import com.example.monew.domain.comment.entity.CommentLikes;
import com.example.monew.domain.comment.repository.CommentLikesRepository;
import com.example.monew.domain.comment.repository.CommentRepository;
import com.example.monew.domain.user.entity.User;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentLikeService {

    private final CommentLikesRepository commentLikesRepository;
    private final CommentRepository commentRepository;
    private final EntityManager entityManager;

    public void like(UUID userId, UUID commentId) {
        Comment comment = commentRepository.findByIdAndIsDeletedFalse(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));

        User user = entityManager.find(User.class, userId);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        if (commentLikesRepository.existsByUser_IdAndComment_Id(userId, commentId)) {
            return;
        }

        commentLikesRepository.save(new CommentLikes(user, comment));
    }

    public void unlike(UUID userId, UUID commentId) {
        if (!commentLikesRepository.existsByUser_IdAndComment_Id(userId, commentId)) {
            return;
        }
        commentLikesRepository.deleteByUser_IdAndComment_Id(userId, commentId);
    }
}
