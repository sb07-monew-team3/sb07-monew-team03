package com.example.monew.domain.comment.unit.service;

import com.example.monew.domain.comment.entity.Comment;
import com.example.monew.domain.comment.entity.CommentLikes;
import com.example.monew.domain.comment.repository.CommentLikesRepository;
import com.example.monew.domain.comment.repository.CommentRepository;
import com.example.monew.domain.comment.service.CommentLikeService;
import com.example.monew.domain.user.entity.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentLikeServiceTest {

    @Mock
    CommentLikesRepository commentLikesRepository;

    @Mock
    CommentRepository commentRepository;

    @Mock
    EntityManager entityManager;

    @InjectMocks
    CommentLikeService commentLikeService;

    @Test
    @DisplayName("좋아요 취소: 댓글이 없으면 404")
    void unlike_throws404_whenCommentNotFound() {
        UUID userId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();

        when(commentRepository.findByIdAndIsDeletedFalse(commentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentLikeService.unlike(userId, commentId))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException e = (ResponseStatusException) ex;
                    assertThat(e.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(e.getReason()).isEqualTo("Comment not found");
                });

        verify(commentLikesRepository, never()).deleteByUserIdAndCommentId(any(), any());
    }
    @Test
    @DisplayName("좋아요: 유저가 없으면 404")
    void like_throws404_whenUserNotFound() {
        UUID userId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();

        Comment comment = mock(Comment.class);
        when(commentRepository.findByIdAndIsDeletedFalse(commentId)).thenReturn(Optional.of(comment));
        when(entityManager.find(User.class, userId)).thenReturn(null);

        assertThatThrownBy(() -> commentLikeService.like(userId, commentId))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException e = (ResponseStatusException) ex;
                    assertThat(e.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(e.getReason()).isEqualTo("User not found");
                });

        verify(commentLikesRepository, never()).save(any());
    }

    @Test
    @DisplayName("좋아요: 이미 좋아요가 있으면 저장하지 않는다")
    void like_doesNothing_whenAlreadyLiked() {
        UUID userId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();

        Comment comment = mock(Comment.class);
        User user = mock(User.class);

        when(commentRepository.findByIdAndIsDeletedFalse(commentId)).thenReturn(Optional.of(comment));
        when(entityManager.find(User.class, userId)).thenReturn(user);
        when(commentLikesRepository.existsByUserIdAndCommentId(userId, commentId)).thenReturn(true);

        commentLikeService.like(userId, commentId);

        verify(commentLikesRepository, never()).save(any());
    }

    @Test
    @DisplayName("좋아요: 좋아요가 없으면 새 좋아요를 저장")
    void like_saves_whenNotExists() {
        UUID userId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();

        Comment comment = mock(Comment.class);
        User user = mock(User.class);

        when(commentRepository.findByIdAndIsDeletedFalse(commentId)).thenReturn(Optional.of(comment));
        when(entityManager.find(User.class, userId)).thenReturn(user);
        when(commentLikesRepository.existsByUserIdAndCommentId(userId, commentId)).thenReturn(false);

        commentLikeService.like(userId, commentId);

        ArgumentCaptor<CommentLikes> captor = ArgumentCaptor.forClass(CommentLikes.class);
        verify(commentLikesRepository).save(captor.capture());

        CommentLikes saved = captor.getValue();
        assertThat(saved.getUser()).isSameAs(user);
        assertThat(saved.getComment()).isSameAs(comment);
    }

    @Test
    @DisplayName("좋아요 취소: 좋아요 없으면 삭제하지 않는다")
    void unlike_doesNothing_whenNotExists() {
        UUID userId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();

        Comment comment = mock(Comment.class);
        User user = mock(User.class);

        when(commentRepository.findByIdAndIsDeletedFalse(commentId)).thenReturn(Optional.of(comment));
        when(entityManager.find(User.class, userId)).thenReturn(user);
        when(commentLikesRepository.existsByUserIdAndCommentId(userId, commentId)).thenReturn(false);

        commentLikeService.unlike(userId, commentId);

        verify(commentLikesRepository, never()).deleteByUserIdAndCommentId(any(), any());
    }

    @Test
    @DisplayName("좋아요 취소: 좋아요가 있으면 삭제한다")
    void unlike_deletes_whenExists() {
        UUID userId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();

        Comment comment = mock(Comment.class);
        User user = mock(User.class);

        when(commentRepository.findByIdAndIsDeletedFalse(commentId)).thenReturn(Optional.of(comment));
        when(entityManager.find(User.class, userId)).thenReturn(user);
        when(commentLikesRepository.existsByUserIdAndCommentId(userId, commentId)).thenReturn(true);

        commentLikeService.unlike(userId, commentId);

        verify(commentLikesRepository).deleteByUserIdAndCommentId(userId, commentId);
    }


    @Test
    @DisplayName("좋아요: 댓글/유저 조회 후 exists 체크 순서대로 호출된다")
    void like_callsInOrder() {
        UUID userId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();

        Comment comment = mock(Comment.class);
        User user = mock(User.class);

        when(commentRepository.findByIdAndIsDeletedFalse(commentId)).thenReturn(Optional.of(comment));
        when(entityManager.find(User.class, userId)).thenReturn(user);
        when(commentLikesRepository.existsByUserIdAndCommentId(userId, commentId)).thenReturn(false);

        commentLikeService.like(userId, commentId);

        InOrder inOrder = inOrder(commentRepository, entityManager, commentLikesRepository);
        inOrder.verify(commentRepository).findByIdAndIsDeletedFalse(commentId);
        inOrder.verify(entityManager).find(User.class, userId);
        inOrder.verify(commentLikesRepository).existsByUserIdAndCommentId(userId, commentId);
        inOrder.verify(commentLikesRepository).save(any(CommentLikes.class));
    }
}
