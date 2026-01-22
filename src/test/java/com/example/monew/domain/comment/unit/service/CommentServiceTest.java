package com.example.monew.domain.comment.unit.service;

import com.example.monew.domain.activity.dto.UserActivityCommentDto;
import com.example.monew.domain.activity.mapper.UserActivityCommentMapper;
import com.example.monew.domain.activity.service.MongoDbService;
import com.example.monew.domain.article.entity.Article;
import com.example.monew.domain.comment.dto.CommentResponse;
import com.example.monew.domain.comment.entity.Comment;
import com.example.monew.domain.comment.repository.CommentLikesRepository;
import com.example.monew.domain.comment.repository.CommentRepository;
import com.example.monew.domain.comment.repository.CommentWithLikeCount;
import com.example.monew.domain.comment.service.CommentService;
import com.example.monew.domain.user.entity.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    CommentRepository commentRepository;

    @Mock
    CommentLikesRepository commentLikesRepository;

    @Mock
    EntityManager entityManager;

    @InjectMocks
    CommentService commentService;

    @Mock
    MongoDbService mongoDbService;

    @Mock
    UserActivityCommentMapper userActivityCommentMapper;


    @Test
    @DisplayName("list(Pageable): createdAt 정렬")
    void list_pageable_createdAt() {
        UUID requesterId = UUID.randomUUID();
        UUID articleId = UUID.randomUUID();

        Article article = mock(Article.class);
        when(article.getId()).thenReturn(articleId);
        when(entityManager.find(Article.class, articleId)).thenReturn(article);

        User user = mock(User.class);
        when(user.getId()).thenReturn(requesterId);

        Comment c1 = new Comment(user, article, "c1", false);
        Comment c2 = new Comment(user, article, "c2", false);

        UUID c1Id = UUID.randomUUID();
        UUID c2Id = UUID.randomUUID();
        ReflectionTestUtils.setField(c1, "id", c1Id);
        ReflectionTestUtils.setField(c2, "id", c2Id);

        Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Comment> page = new PageImpl<>(List.of(c1, c2), pageable, 2);

        when(commentRepository.findByArticle_IdAndIsDeletedFalse(articleId, pageable)).thenReturn(page);
        when(commentLikesRepository.countByCommentIds(List.of(c1Id, c2Id))).thenReturn(Map.of(c1Id, 2L, c2Id, 0L));
        when(commentLikesRepository.findLikedCommentIds(requesterId, List.of(c1Id, c2Id))).thenReturn(Set.of(c1Id));

        Page<CommentResponse> result = commentService.list(requesterId, articleId, pageable);

        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("list(Pageable): likeCount 정렬")
    void list_pageable_likeCount() {
        UUID requesterId = UUID.randomUUID();
        UUID articleId = UUID.randomUUID();

        Article article = mock(Article.class);
        when(article.getId()).thenReturn(articleId);
        when(entityManager.find(Article.class, articleId)).thenReturn(article);

        User user = mock(User.class);
        when(user.getId()).thenReturn(requesterId);

        Comment comment = new Comment(user, article, "c", false);
        UUID commentId = UUID.randomUUID();
        ReflectionTestUtils.setField(comment, "id", commentId);

        CommentWithLikeCount row = mock(CommentWithLikeCount.class);
        when(row.comment()).thenReturn(comment);
        when(row.likeCount()).thenReturn(10L);

        Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "likeCount"));
        Page<CommentWithLikeCount> page = new PageImpl<>(List.of(row), pageable, 1);

        when(commentRepository.findByArticleIdOrderByLikeCount(articleId, pageable, Sort.Direction.DESC)).thenReturn(page);
        when(commentLikesRepository.findLikedCommentIds(requesterId, List.of(commentId))).thenReturn(Set.of(commentId));

        Page<CommentResponse> result = commentService.list(requesterId, articleId, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("list(params): orderBy/direction/page/limit 모두 null이면 기본값 적용")
    void list_params_defaults() {
        UUID requesterId = UUID.randomUUID();
        UUID articleId = UUID.randomUUID();

        Article article = mock(Article.class);
        when(entityManager.find(Article.class, articleId)).thenReturn(article);

        when(commentRepository.findByArticle_IdAndIsDeletedFalse(eq(articleId), any(Pageable.class)))
                .thenReturn(Page.empty());

        when(commentLikesRepository.countByCommentIds(anyList())).thenReturn(Map.of());
        when(commentLikesRepository.findLikedCommentIds(eq(requesterId), anyList())).thenReturn(Set.of());

        commentService.list(requesterId, articleId, null, null, null, null);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(commentRepository).findByArticle_IdAndIsDeletedFalse(eq(articleId), captor.capture());

        Pageable used = captor.getValue();
        assertThat(used.getPageNumber()).isEqualTo(0);
        assertThat(used.getSort().getOrderFor("createdAt")).isNotNull();
        assertThat(used.getSort().getOrderFor("createdAt").getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    @DisplayName("list(params): orderBy가 허용되지 않으면 예외")
    void list_params_invalidOrderBy_throws() {
        UUID requesterId = UUID.randomUUID();
        UUID articleId = UUID.randomUUID();

        assertThatThrownBy(() -> commentService.list(requesterId, articleId, "invalid", Sort.Direction.DESC, 0, 5))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("list(params): page 음수면 0으로 보정")
    void list_params_negativePage_toZero() {
        UUID requesterId = UUID.randomUUID();
        UUID articleId = UUID.randomUUID();

        Article article = mock(Article.class);
        when(entityManager.find(Article.class, articleId)).thenReturn(article);

        when(commentRepository.findByArticle_IdAndIsDeletedFalse(eq(articleId), any(Pageable.class)))
                .thenReturn(Page.empty());

        when(commentLikesRepository.countByCommentIds(anyList())).thenReturn(Map.of());
        when(commentLikesRepository.findLikedCommentIds(eq(requesterId), anyList())).thenReturn(Set.of());

        commentService.list(requesterId, articleId, "createdAt", Sort.Direction.DESC, -99, 5);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(commentRepository).findByArticle_IdAndIsDeletedFalse(eq(articleId), captor.capture());

        Pageable used = captor.getValue();
        assertThat(used.getPageNumber()).isEqualTo(0);
    }

    @Test
    @DisplayName("list(params): limit 보정 (음수→1, 과대→50)")
    void list_params_limitClamp() {
        UUID requesterId = UUID.randomUUID();
        UUID articleId = UUID.randomUUID();

        Article article = mock(Article.class);
        when(entityManager.find(Article.class, articleId)).thenReturn(article);

        when(commentRepository.findByArticle_IdAndIsDeletedFalse(eq(articleId), any(Pageable.class)))
                .thenReturn(Page.empty());

        when(commentLikesRepository.countByCommentIds(anyList())).thenReturn(Map.of());
        when(commentLikesRepository.findLikedCommentIds(eq(requesterId), anyList())).thenReturn(Set.of());

        commentService.list(requesterId, articleId, "createdAt", Sort.Direction.DESC, 0, -1);

        ArgumentCaptor<Pageable> captor1 = ArgumentCaptor.forClass(Pageable.class);
        verify(commentRepository).findByArticle_IdAndIsDeletedFalse(eq(articleId), captor1.capture());

        Pageable used1 = captor1.getValue();
        assertThat(used1.getPageSize()).isEqualTo(1);

        clearInvocations(commentRepository);

        when(commentRepository.findByArticle_IdAndIsDeletedFalse(eq(articleId), any(Pageable.class)))
                .thenReturn(Page.empty());

        commentService.list(requesterId, articleId, "createdAt", Sort.Direction.DESC, 0, 9999);

        ArgumentCaptor<Pageable> captor2 = ArgumentCaptor.forClass(Pageable.class);
        verify(commentRepository).findByArticle_IdAndIsDeletedFalse(eq(articleId), captor2.capture());

        Pageable used2 = captor2.getValue();
        assertThat(used2.getPageSize()).isEqualTo(50);
    }

    @Test
    @DisplayName("create: 정상 생성")
    void create_success() {
        UUID userId = UUID.randomUUID();
        UUID articleId = UUID.randomUUID();

        User user = mock(User.class);
        Article article = mock(Article.class);

        when(user.getId()).thenReturn(userId);
        when(article.getId()).thenReturn(articleId);

        when(entityManager.find(User.class, userId)).thenReturn(user);
        when(entityManager.find(Article.class, articleId)).thenReturn(article);
        doNothing().when(mongoDbService)
                .insertUserActivityComment(any(UUID.class),any(UserActivityCommentDto.class));
        when(userActivityCommentMapper.toUserActivityCommentDto(any(Comment.class))).thenReturn(
                new UserActivityCommentDto(
                        userId, articleId, "hi", userId,"hi",
                        "hi",
                        3,
                        Instant.now()
                )
        );

        Comment saved = new Comment(user, article, "hi", false);
        UUID savedId = UUID.randomUUID();
        ReflectionTestUtils.setField(saved, "id", savedId);

        when(commentRepository.save(any(Comment.class))).thenReturn(saved);
        when(commentLikesRepository.countByComment_Id(savedId)).thenReturn(1L);
        when(commentLikesRepository.existsByUserIdAndCommentId(userId, savedId)).thenReturn(true);

        CommentResponse res = commentService.create(userId, articleId, "  hi ");

        assertThat(res).isNotNull();
    }

    @Test
    @DisplayName("create: content null → 400")
    void create_contentNull() {
        UUID userId = UUID.randomUUID();
        UUID articleId = UUID.randomUUID();

        assertThatThrownBy(() -> commentService.create(userId, articleId, null))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    @DisplayName("update: 본인 댓글 수정")
    void update_owner_success() {
        UUID userId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();

        User user = mock(User.class);
        when(user.getId()).thenReturn(userId);

        Article article = mock(Article.class);
        when(article.getId()).thenReturn(UUID.randomUUID());

        Comment comment = new Comment(user, article, "old", false);
        ReflectionTestUtils.setField(comment, "id", commentId);

        when(commentRepository.findByIdAndIsDeletedFalse(commentId)).thenReturn(Optional.of(comment));
        when(commentLikesRepository.countByComment_Id(commentId)).thenReturn(0L);
        when(commentLikesRepository.existsByUserIdAndCommentId(userId, commentId)).thenReturn(false);
        doNothing().when(mongoDbService).updateUserActivityComment(any(CommentResponse.class));
        CommentResponse res = commentService.update(userId, commentId, " new ");

        assertThat(res).isNotNull();
    }

    @Test
    @DisplayName("update: 남의 댓글 → 403")
    void update_forbidden() {
        UUID userId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();

        User owner = mock(User.class);
        when(owner.getId()).thenReturn(UUID.randomUUID());

        Article article = mock(Article.class);

        Comment comment = new Comment(owner, article, "c", false);
        ReflectionTestUtils.setField(comment, "id", commentId);

        when(commentRepository.findByIdAndIsDeletedFalse(commentId)).thenReturn(Optional.of(comment));

        assertThatThrownBy(() -> commentService.update(userId, commentId, "x"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN));
    }

    @Test
    @DisplayName("softDelete: userId null 허용")
    void softDelete_userIdNull() {
        UUID commentId = UUID.randomUUID();

        User user = mock(User.class);
        Article article = mock(Article.class);

        Comment comment = new Comment(user, article, "c", false);
        ReflectionTestUtils.setField(comment, "id", commentId);

        when(commentRepository.findByIdAndIsDeletedFalse(commentId)).thenReturn(Optional.of(comment));
        doNothing().when(mongoDbService).updateWhenCommentDelete(any());

        commentService.softDelete(null, commentId);

        assertThat(comment.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("hardDelete: repository.delete 호출")
    void hardDelete_success() {
        UUID userId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();

        User user = mock(User.class);
        when(user.getId()).thenReturn(userId);

        Article article = mock(Article.class);

        Comment comment = new Comment(user, article, "c", false);
        ReflectionTestUtils.setField(comment, "id", commentId);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        commentService.hardDelete(userId, commentId);

        verify(commentRepository).delete(comment);
    }
}
