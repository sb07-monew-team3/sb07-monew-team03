package com.example.monew.domain.user.service;

import com.example.monew.domain.user.dto.UserDto;
import com.example.monew.domain.user.dto.UserLoginRequest;
import com.example.monew.domain.user.dto.UserRegisterRequest;
import com.example.monew.domain.user.dto.UserUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class IUserService implements UserService{

    @Override
    public UserDto createUser(UserRegisterRequest request) {
        return null;
    }

    @Override
    public UserDto loginUser(UserLoginRequest request) {
        return null;
    }

    @Override
    public void deleteUserLogic(UUID userId) {

    }

    @Override
    public UserDto updateUser(UUID userId, UserUpdateRequest request) {
        return null;
    }

    @Override
    public void deleteUserHard(UUID userId) {

    }
}
