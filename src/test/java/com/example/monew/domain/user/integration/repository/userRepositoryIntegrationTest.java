package com.example.monew.domain.user.integration.repository;

import com.example.monew.domain.user.dto.UserDto;
import com.example.monew.domain.user.dto.UserRegisterRequest;
import com.example.monew.domain.user.entity.User;
import com.example.monew.domain.user.repository.UserRepository;
import com.example.monew.domain.user.service.UserService;
import com.example.monew.domain.user.util.TestFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@DisplayName("User Repository Slice Test")
@Transactional
@TestPropertySource(properties = "scheduler.enabled=false")
public class userRepositoryIntegrationTest {


    @Autowired
    UserRepository userRepository;

    @Autowired
    UserService userService;

    private final TestFixture testFixture = new TestFixture();

    private User user1;
    private User user2;


    @Test
    @DisplayName("[정상 케이스] 논리 삭제 유저 조회 ")
    void findDeletedUser_validUser_success(){

        //given
        UserRegisterRequest userRegisterRequest = testFixture.userRegisterRequestFactory();
        UserRegisterRequest userRegisterRequest2 = testFixture.userRegisterRequestFactory();

        UserDto user1 = userService.createUser(userRegisterRequest);
        UserDto user2 = userService.createUser(userRegisterRequest2);
        userService.deleteUserLogic(user1.id());


        //when
        List<User> actualResult = userRepository.findLogicDeleteUser();

        //then
        assertThat(actualResult.size()).isEqualTo(1);
        assertThat(actualResult.get(0).getId()).isEqualTo(user1.id());

    }

    @Test
    @DisplayName("[정상 케이스] 유저 이메일 중복 체크")
    void isEmailExist_validUser_success(){
        //given
        UserRegisterRequest userRegisterRequest = testFixture.userRegisterRequestFactory();
        UserRegisterRequest userRegisterRequest2 = testFixture.userRegisterRequestFactory();

        UserDto user1 = userService.createUser(userRegisterRequest);
        UserDto user2 = userService.createUser(userRegisterRequest2);

        //when
        boolean actualResult = userRepository.isEmailExist(userRegisterRequest.email());
        boolean actualResult2 = userRepository.isEmailExist(UUID.randomUUID().toString());

        //then
        assertThat(actualResult).isTrue();
        assertThat(actualResult2).isFalse();

    }
}
