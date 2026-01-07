package com.example.monew.domain.comment.repository;

import com.example.monew.domain.comment.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {

    Page<Comment> findByArticle_IdAndIsDeletedFalse(UUID articleId, Pageable pageable);

    Optional<Comment> findByIdAndIsDeletedFalse(UUID commentId);
}
