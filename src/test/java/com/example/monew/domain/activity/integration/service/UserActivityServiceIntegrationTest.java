package com.example.monew.domain.activity.integration.service;

import com.example.monew.domain.activity.dto.UserActivityDto;
import com.example.monew.domain.activity.service.UserActivityService;
import com.example.monew.domain.article.entity.Article;
import com.example.monew.domain.article.repository.ArticleRepository;
import com.example.monew.domain.article.service.ArticleService;
import com.example.monew.domain.comment.dto.CommentResponse;
import com.example.monew.domain.comment.repository.CommentLikesRepository;
import com.example.monew.domain.comment.repository.CommentRepository;
import com.example.monew.domain.comment.service.CommentLikeService;
import com.example.monew.domain.comment.service.CommentService;
import com.example.monew.domain.interest.dto.InterestDto;
import com.example.monew.domain.interest.entity.Interest;
import com.example.monew.domain.interest.entity.Subscription;
import com.example.monew.domain.interest.repository.InterestRepository;
import com.example.monew.domain.interest.service.InterestService;
import com.example.monew.domain.interest.service.SubscriptionService;
import com.example.monew.domain.user.dto.UserDto;
import com.example.monew.domain.user.dto.UserRegisterRequest;
import com.example.monew.domain.user.entity.User;
import com.example.monew.domain.user.service.UserService;
import com.example.monew.domain.user.util.TestFixture;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@DisplayName("User Activity Service Integration Test")
@Transactional
@TestPropertySource(properties = "scheduler.enabled=false")
public class UserActivityServiceIntegrationTest {

    @Autowired
    private UserActivityService userActivityService;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentLikesRepository commentLikesRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private CommentService commentService;

    @Autowired
    private ArticleService articleService;

    @Autowired
    private InterestService interestService;

    @Autowired
    private InterestRepository interestRepository;

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private CommentLikeService commentLikeService;

    @Autowired
    private TestFixture testFixture;

    private UserDto userDto;

    @BeforeEach
    void setUp() {
        UserRegisterRequest userRegisterRequest = testFixture.userRegisterRequestFactory();
        userDto= userService.createUser(userRegisterRequest);
    }

    @Test
    @DisplayName("[정상 케이스] 유저 활동 내역 댓글 정상 조회")
    void getUserActivity_validComment_success(){
        //given

        InterestDto interestDto = interestService.create(testFixture.interestRegisterRequestFactory());
        Interest interest = interestRepository.findById(interestDto.id()).orElseThrow();
        Article article = testFixture.articleFactory(
                List.of(
                        interest
                )
        );
        Article saved = articleRepository.save(article);
        CommentResponse comment = commentService.create(userDto.id(), saved.getId(), "siuuu");
        subscriptionService.subscribe(interest.getId(),userDto.id());
        commentLikeService.like(userDto.id(),comment.getId());

        //when
        UserActivityDto actualResult = userActivityService.getUserActivity(userDto.id());

        //then
        assertThat(actualResult.email()).isEqualTo(userDto.email());
        assertThat(actualResult.comments().length).isEqualTo(1);
        assertThat(actualResult.comments()[0].content()).isEqualTo(comment.getContent());
        assertThat(actualResult.commentLikes().length).isEqualTo(1);

    }




}
