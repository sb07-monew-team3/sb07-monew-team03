package com.example.monew.domain.comment.repository;

import com.example.monew.domain.comment.entity.CommentLikes;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CommentLikesRepository extends JpaRepository<CommentLikes, UUID> {

    boolean existsByUser_IdAndComment_Id(UUID userId, UUID commentId);

    Optional<CommentLikes> findByUser_IdAndComment_Id(UUID userId, UUID commentId);

    long countByComment_Id(UUID commentId);

    void deleteByUser_IdAndComment_Id(UUID userId, UUID commentId);
}
