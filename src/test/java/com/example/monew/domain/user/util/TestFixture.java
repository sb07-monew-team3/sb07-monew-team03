package com.example.monew.domain.user.util;


import com.example.monew.domain.activity.dto.UserActivityDto;
import com.example.monew.domain.article.entity.Article;
import com.example.monew.domain.comment.dto.CommentCreateRequest;
import com.example.monew.domain.interest.dto.InterestRegisterRequest;
import com.example.monew.domain.interest.entity.Interest;
import com.example.monew.domain.user.dto.*;
import com.example.monew.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Content;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Component
public class TestFixture {


    public User userFactory(){
        return new User(randomString()+"@naver.com",randomString(),randomString(),null);
    }
    public UserRegisterRequest userRegisterRequestFactory(){

        return new UserRegisterRequest(
                randomString()+"@naver.com",
                randomString(),
                "AaBb11!!"
        );
    }

    public UserUpdateRequest userUpdateRequestFactory(){
        return new UserUpdateRequest(randomString());
    }

    public CommentCreateRequest commentCreateRequestFactory(UUID articleId,UUID userId){
        return new CommentCreateRequest(
                articleId,
                userId,
                randomString()
                );
    }
    public Article articleFactory(List<Interest> interests){
        return new Article(
                randomString(),
                randomString(),
                randomString(),
                LocalDateTime.now(),
                randomString(),
                false,
                Instant.now().minus(Duration.ofMinutes( new Random().nextInt(100))),
                interests
        );
    }

    public InterestRegisterRequest interestRegisterRequestFactory(){
        return new InterestRegisterRequest(
                randomString(),
                List.of(randomString(),randomString(),randomString())
        );
    }
    public UserActivityDto userActivityDtoFactory(){
        return new UserActivityDto(
                UUID.randomUUID(),
                randomString()+ "@naver.com",
                randomString(),
                Instant.now(),
                null,
                null,
                null,
                null
        );
    }


    String randomString(){
        return UUID.randomUUID().toString().substring(0,10);
    }


}
