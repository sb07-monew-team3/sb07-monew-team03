package com.example.monew.domain.activity.integration.service;

import com.example.monew.domain.activity.dto.UserActivityArticleViewDto;
import com.example.monew.domain.activity.dto.UserActivityCommentDto;
import com.example.monew.domain.activity.dto.UserActivityDto;
import com.example.monew.domain.activity.service.MongoDbService;
import com.example.monew.domain.article.service.ArticleService;
import com.example.monew.domain.comment.dto.CommentResponse;
import com.example.monew.domain.user.dto.UserDto;
import com.example.monew.domain.user.entity.User;
import com.example.monew.domain.user.mapper.UserMapper;
import com.example.monew.domain.user.util.TestFixture;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class MongoDbServiceIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(MongoDbServiceIntegrationTest.class);
    @Autowired
    MongoDbService mongoDbService;

    @Autowired
    TestFixture testFixture;

    @Autowired
    UserMapper userMapper;


    @Test
    @DisplayName("[정상 케이스] 유저 활동 내역 등록 성공")
    void insertUserActivity_validUserActivity_success(){

        //given
        User user = testFixture.userFactory();
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(user, "createdAt", Instant.now());
        UserDto userDto = userMapper.toDto(user);
        log.info(userDto.toString());
        mongoDbService.insertUserActivity(userDto);

        //when
        UserActivityDto actualResult = mongoDbService.getUserActivity(userDto.id());

        //then
        assertThat(actualResult.id()).isEqualTo(userDto.id());
    }

    @Test
    @DisplayName("[정상 케이스] 유저 활동 내역 조회 성공")
    void getUserActivity_validUserActivity_success(){
        //given
        User user = testFixture.userFactory();
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(user, "createdAt", Instant.now());
        UserDto userDto = userMapper.toDto(user);
        log.info(userDto.toString());
        mongoDbService.insertUserActivity(userDto);

        //when
        UserActivityDto actualResult = mongoDbService.getUserActivity(userDto.id());
        //then
        assertThat(actualResult.id()).isEqualTo(userDto.id());

    }

    @Test
    @DisplayName("[정상 케이스] 유저 활동 내역 기사 조회 삽입")
    void insertArticleView_validArticleView_success(){
        //given
        User user = testFixture.userFactory();
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(user, "createdAt", Instant.now());
        UserDto userDto = userMapper.toDto(user);
        mongoDbService.insertUserActivity(userDto);

        UserActivityArticleViewDto userActivityArticleViewDto = new UserActivityArticleViewDto(
                UUID.randomUUID(),
                userDto.id(),
                Instant.now(),
                UUID.randomUUID(),
                "source",
                "sourceUrl",
                "title",
                LocalDateTime.now(),
                "summary",
                0,
                0
        );

        //when
        mongoDbService.insertUserActivityArticleView(userDto.id(), userActivityArticleViewDto);
        var actualResult = mongoDbService.getUserActivity(userDto.id());

        //then
        assertThat(actualResult.articleViews().length).isEqualTo(1);
    }

    @Test
    @DisplayName("[정상 케이스] 유저 활동 내역 댓글 작성")
    void insertUserActivityComment_validComment_success(){

        //given
        User user = testFixture.userFactory();
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(user, "createdAt", Instant.now());
        UserDto userDto = userMapper.toDto(user);
        mongoDbService.insertUserActivity(userDto);

        UserActivityCommentDto commentDto = new UserActivityCommentDto(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "title",
                userDto.id(),
                "nickname",
                "content",
                0,
                Instant.now()
        );

        //when
        mongoDbService.insertUserActivityComment(userDto.id(), commentDto);
        var actualResult = mongoDbService.getUserActivity(userDto.id());

        //then
        assertThat(actualResult.comments().length).isEqualTo(1);
        assertThat(actualResult.comments()[0].id()).isEqualTo(commentDto.id());

    }

    @Test
    @DisplayName("[정상 케이스] 유저 활동 내용 comment 수정")
    void updateUserActivityComment_validComment_success(){
        //given
        User user = testFixture.userFactory();
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(user, "createdAt", Instant.now());
        UserDto userDto = userMapper.toDto(user);
        mongoDbService.insertUserActivity(userDto);

        UserActivityCommentDto commentDto = new UserActivityCommentDto(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "title",
                userDto.id(),
                "nickname",
                "content",
                0,
                Instant.now()
        );
        mongoDbService.insertUserActivityComment(userDto.id(), commentDto);
        String editContent = "editContent";
        CommentResponse commentResponse = new CommentResponse(
                commentDto.id(),
                commentDto.articleId(),
                commentDto.userId(),
                commentDto.userNickname(),
                editContent,
                commentDto.createdAt(),
                commentDto.likeCount(),
                true
        );

        //when
         mongoDbService.updateUserActivityComment(commentResponse);
        var actualResult = mongoDbService.getUserActivity(userDto.id());

        //then
        assertThat(actualResult.comments()[0].content()).isEqualTo(editContent);

    }
}
