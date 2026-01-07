package com.example.monew.domain.user.service;

import com.example.monew.domain.user.dto.UserDto;
import com.example.monew.domain.user.dto.UserLoginRequest;
import com.example.monew.domain.user.dto.UserRegisterRequest;
import com.example.monew.domain.user.dto.UserUpdateRequest;

import java.util.UUID;

public interface UserService {

    UserDto createUser(UserRegisterRequest request);
    UserDto loginUser(UserLoginRequest request);
    void deleteUserLogic(UUID userId) ;
    UserDto updateUser(UUID userId, UserUpdateRequest request);
    void deleteUserHard(UUID userId);
}
