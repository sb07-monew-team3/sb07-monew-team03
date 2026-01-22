package com.example.monew.domain.comment.repository;

import com.example.monew.domain.comment.entity.Comment;
import com.example.monew.domain.comment.repository.CommentRepositoryCustom;
import com.example.monew.domain.comment.entity.CommentLikes;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CommentLikesRepository extends JpaRepository<CommentLikes, UUID> ,CommentLikesRepositoryCustom{

    boolean existsByUserIdAndCommentId(UUID userId, UUID commentId);

    long countByComment_Id(UUID commentId);

    void deleteByUserIdAndCommentId(UUID userId, UUID commentId);

    Optional<CommentLikes> findByCommentIdAndUserId(UUID commentId, UUID userId);

    List<CommentLikes> findAllByCommentId(UUID commentId);
}
