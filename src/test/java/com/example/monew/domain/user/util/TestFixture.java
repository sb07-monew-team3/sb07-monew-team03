package com.example.monew.domain.user.util;


import com.example.monew.domain.user.dto.UserDto;
import com.example.monew.domain.user.dto.UserLoginRequest;
import com.example.monew.domain.user.dto.UserRegisterRequest;
import com.example.monew.domain.user.dto.UserUpdateRequest;
import com.example.monew.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Content;
import org.springframework.stereotype.Component;

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




    String randomString(){
        return UUID.randomUUID().toString().substring(0,10);
    }


}
