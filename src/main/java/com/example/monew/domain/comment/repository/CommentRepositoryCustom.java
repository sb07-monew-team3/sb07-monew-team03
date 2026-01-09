package com.example.monew.domain.comment.repository;

import com.example.monew.domain.comment.entity.Comment;

import java.util.List;
import java.util.UUID;

public interface CommentRepositoryCustom {

    List<Comment> getCommentsByUserId(UUID userId);
    Long countByArticleId(UUID articleId);
}
