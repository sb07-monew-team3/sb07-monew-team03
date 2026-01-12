package com.example.monew.domain.comment.repository;

import com.example.monew.domain.comment.entity.Comment;

public record CommentWithLikeCount(Comment comment, long likeCount) {
}
