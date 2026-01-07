package com.example.monew.domain.user.unit.service;


import com.example.monew.domain.user.dto.UserDto;
import com.example.monew.domain.user.dto.UserLoginRequest;
import com.example.monew.domain.user.dto.UserRegisterRequest;
import com.example.monew.domain.user.dto.UserUpdateRequest;
import com.example.monew.domain.user.entity.User;
import com.example.monew.domain.user.mapper.UserMapper;
import com.example.monew.domain.user.repository.UserRepository;
import com.example.monew.domain.user.service.IUserService;
import com.example.monew.domain.user.service.UserService;
import com.example.monew.domain.user.util.TestFixture;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Test")

public class UserServiceUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private IUserService userService;

    private final TestFixture testFixture = new TestFixture();
    private User user;
    private UserDto userDto;
    @BeforeEach
    void setUp() {
        user = testFixture.userFactory();
        userDto = new UserDto(user.getId(),user.getEmail(),user.getNickName(),user.getCreatedAt());

    }

    @Test
    @DisplayName("[정상 케이스] 유저 등록")
    void createUser_validUser_success() {


        //given
        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<String> emailArgumentCaptor = ArgumentCaptor.forClass(String.class);
        given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(userRepository.isEmailExist(any(String.class))).willReturn(false);
        given(userMapper.toDto(any(User.class))).willReturn(userDto);

        UserRegisterRequest request = new UserRegisterRequest(user.getEmail(),user.getNickName(),user.getPassword());

        //when

        var actualResult = userService.createUser(request);
        var expectResult = userDto;

        //then
        then(userRepository).should(times(1)).save(userArgumentCaptor.capture());
        then(userRepository).should(times(1)).isEmailExist(emailArgumentCaptor.capture());
        then(userMapper).should(times(1)).toDto(any(User.class));

        var isEmailExist = emailArgumentCaptor.getValue();
        var saveUser = userArgumentCaptor.getValue();
        var expectSaveUser = user;

        assertThat(saveUser.getId()).isEqualTo(expectSaveUser.getId());
        assertThat(actualResult.id()).isEqualTo(expectResult.id());
        assertThat(isEmailExist).isEqualTo(user.getEmail());

    }


    @Test
    @DisplayName("[정상 케이스] 유저 로그인")
    void loginUser_validUser_success() {
        //given
        ArgumentCaptor<String> emailArgumentCaptor = ArgumentCaptor.forClass(String.class);
        UserLoginRequest request = new UserLoginRequest(user.getEmail(),user.getPassword());

        given(userRepository.findByEmail(any(String.class))).willReturn(Optional.of(user));
        given(userMapper.toDto(any(User.class))).willReturn(userDto);

        //when
        var actualResult = userService.loginUser(request);
        var expectResult = userDto;

        //then
        then(userRepository).should(times(1)).findByEmail(emailArgumentCaptor.capture());
        then(userMapper).should(times(1)).toDto(any(User.class));

        assertThat(actualResult.id()).isEqualTo(expectResult.id());
        assertThat(emailArgumentCaptor.getValue()).isEqualTo(user.getEmail());

    }

    @Test
    @DisplayName("[정상 케이스] 유저 논리 삭제")
    void deleteUserLogic_deleteUser_success(){
        //given
        ArgumentCaptor<UUID> idArgumentCaptor = ArgumentCaptor.forClass(UUID.class);
        UUID userId = UUID.randomUUID();
        given(userRepository.findById(any(UUID.class))).willReturn(Optional.of(user));

        //when
        userService.deleteUserLogic(userId);
        var actualResult = user.getDeletedAt()!=null;

        //then
        then(userRepository).should(times(1)).findById(idArgumentCaptor.capture());

        assertThat(actualResult).isTrue();
        assertThat(idArgumentCaptor.getValue()).isEqualTo(userId);
    }

    @Test
    @DisplayName("[정상 케이스] 유저 논리 삭제 1일 후 물리 삭제")
    void deleteUserPhysics_deleteUser_batch_success(){
        //given
        ArgumentCaptor<UUID> idArgumentCaptor = ArgumentCaptor.forClass(UUID.class);
        //when

        //then

    }

    @Test
    @DisplayName("[정상 케이스] 유저 물리 삭제")
    void deleteUserPhysics_deleteUser_success(){

        //given
        ArgumentCaptor<UUID> idArgumentCaptor = ArgumentCaptor.forClass(UUID.class);
        UUID userId = UUID.randomUUID();
        given(userRepository.findById(any(UUID.class))).willReturn(Optional.of(user));
        willDoNothing().given(userRepository).deleteById(any(UUID.class));
        //when
        userService.deleteUserHard(userId);

        //then
        then(userRepository).should(times(1)).findById(any(UUID.class));
        then(userRepository).should(times(1)).deleteById(idArgumentCaptor.capture());

        assertThat(idArgumentCaptor.getValue()).isEqualTo(userId);
    }

    @Test
    @DisplayName("[정상 케이스 ] 유저 닉네임 수정")
    void updateUser_nickname_success(){

        //given
        ArgumentCaptor<UUID> idArgumentCaptor = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);

        UUID userId = UUID.randomUUID();
        String newNickName ="Siuuu";
        UserDto expectedDto = new UserDto(user.getId(),user.getEmail(),newNickName,user.getCreatedAt());
        UserUpdateRequest request = new UserUpdateRequest(newNickName);

        given(userRepository.findById(any(UUID.class))).willReturn(Optional.of(user));
        given(userMapper.toDto(any(User.class))).willReturn(expectedDto);

        //when
        userService.updateUser(userId, request);

        var actualResult = user.getNickName();

        //then
        then(userRepository).should(times(1)).findById(idArgumentCaptor.capture());
        then(userMapper).should(times(1)).toDto(userArgumentCaptor.capture());

        assertThat(actualResult).isEqualTo(newNickName);
        assertThat(idArgumentCaptor.getValue()).isEqualTo(userId);
        assertThat(userArgumentCaptor.getValue().getNickName()).isEqualTo(newNickName);
    }




}
