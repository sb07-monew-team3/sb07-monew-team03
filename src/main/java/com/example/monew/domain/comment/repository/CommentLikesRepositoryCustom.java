package com.example.monew.domain.comment.repository;

import com.example.monew.domain.comment.entity.CommentLikes;

import java.util.List;
import java.util.UUID;

public interface CommentLikesRepositoryCustom {

    List<CommentLikes> getCommentLikesByUserId(UUID userId);
    Long countByCommentId(UUID commentId);
}
