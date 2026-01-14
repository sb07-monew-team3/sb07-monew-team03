package com.example.monew.domain.user.integration.service;

import com.example.monew.domain.user.dto.UserDto;
import com.example.monew.domain.user.dto.UserLoginRequest;
import com.example.monew.domain.user.dto.UserRegisterRequest;
import com.example.monew.domain.user.entity.User;
import com.example.monew.domain.user.repository.UserRepository;
import com.example.monew.domain.user.service.UserService;
import com.example.monew.domain.user.util.TestFixture;
import com.example.monew.global.exception.domain.user.UserNotExistException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@DisplayName("User Service Integration Test")
@Transactional
@TestPropertySource(properties = "scheduler.enabled=false")
public class UserServiceIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(UserServiceIntegrationTest.class);
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private TestFixture testFixture;


    @Test
    @DisplayName("[정상 케이스] 유저 등록 성공")
    void createUser_validUser_success(){
        //given
        UserRegisterRequest userRegisterRequest = testFixture.userRegisterRequestFactory();

        //when
        var actualResult = userService.createUser(userRegisterRequest);
        var expectedResultEmail = userRegisterRequest.email();
        var expectedResultNickName = userRegisterRequest.nickname();

        var actualUser = userRepository.findById(actualResult.id()).orElseThrow(()-> new UserNotExistException(actualResult.id()));

        //then
        assertThat(actualResult.email()).isEqualTo(expectedResultEmail);
        assertThat(actualResult.nickname()).isEqualTo(expectedResultNickName);
        assertThat(actualUser.getPassword()).isEqualTo(userRegisterRequest.password());

    }

    @Test
    @DisplayName("[정상 케이스] 유저 로그인 성공")
    void loginUser_validUser_success(){
        //given
        UserRegisterRequest userRegisterRequest = testFixture.userRegisterRequestFactory();
        UserDto userDto = userService.createUser(userRegisterRequest);
        UserLoginRequest userLoginRequest = new UserLoginRequest(userDto.email(),userRegisterRequest.password());

        //when
        var actualUserDto = userService.loginUser(userLoginRequest);
        var actualUser = userRepository.findById(userDto.id()).orElseThrow(() -> new UserNotExistException(userDto.id()));

        //then
        assertThat(actualUserDto.email()).isEqualTo(userDto.email());
        assertThat(actualUserDto.nickname()).isEqualTo(userDto.nickname());
        assertThat(actualUser.getId()).isEqualTo(userDto.id());

    }

    @Test
    @DisplayName("[정상 케이스] 유저 논리 삭제")
    void deleteUserLogic_validUser_success(){
        //given
        UserRegisterRequest userRegisterRequest = testFixture.userRegisterRequestFactory();
        UserDto userDto = userService.createUser(userRegisterRequest);

        //when
        userService.deleteUserLogic(userDto.id());
        var actualUser = userRepository.findById(userDto.id());

        //then
        assertThat(actualUser.isPresent()).isTrue();
        assertThat(actualUser.get().getDeletedAt()).isNotNull();

    }

    @Test
    @DisplayName("[정상 케이스] 유저 물리 삭제")
    void deleteUserPhysics_validUser_success(){
        //given
        UserRegisterRequest userRegisterRequest = testFixture.userRegisterRequestFactory();
        UserDto userDto = userService.createUser(userRegisterRequest);

        //when
        userService.deleteUserHard(userDto.id());
        var actualUser = userRepository.findById(userDto.id());

        //then
        assertThat(actualUser.isPresent()).isFalse();
    }


}
