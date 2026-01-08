package com.example.monew.domain.comment.repository;

import com.example.monew.domain.comment.entity.CommentLikes;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CommentLikesRepository extends JpaRepository<CommentLikes, UUID> {

    boolean existsByUserIdAndCommentId(UUID userId, UUID commentId);

    long countByComment_Id(UUID commentId);

    void deleteByUserIdAndCommentId(UUID userId, UUID commentId);
}
