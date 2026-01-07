package com.example.monew.domain.user.service;

import com.example.monew.domain.user.dto.UserDto;
import com.example.monew.domain.user.dto.UserLoginRequest;
import com.example.monew.domain.user.dto.UserRegisterRequest;
import com.example.monew.domain.user.dto.UserUpdateRequest;
import com.example.monew.domain.user.entity.User;
import com.example.monew.domain.user.mapper.UserMapper;
import com.example.monew.domain.user.repository.UserRepository;
import com.example.monew.global.exception.domain.user.UserEmailExistException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class IUserService implements UserService{

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserDto createUser(UserRegisterRequest request) {
        String email = request.email();
        if( userRepository.isEmailExist(email)) throw new UserEmailExistException(email);
        User save = userRepository.save(new User(request.email(), request.nickname(), request.password(), null));
        return userMapper.toDto(save);
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
