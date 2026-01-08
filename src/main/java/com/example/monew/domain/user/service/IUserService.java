package com.example.monew.domain.user.service;

import com.example.monew.domain.user.dto.UserDto;
import com.example.monew.domain.user.dto.UserLoginRequest;
import com.example.monew.domain.user.dto.UserRegisterRequest;
import com.example.monew.domain.user.dto.UserUpdateRequest;
import com.example.monew.domain.user.entity.User;
import com.example.monew.domain.user.mapper.UserMapper;
import com.example.monew.domain.user.repository.UserRepository;
import com.example.monew.global.exception.domain.user.UserEmailExistException;
import com.example.monew.global.exception.domain.user.UserNotExistException;
import com.example.monew.global.exception.domain.user.UserValidationFailException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
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

        String email = request.email();
        String password = request.password();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserValidationFailException(email));
        if(!user.getPassword().equals(password)||
                user.getDeletedAt()!=null
        ) throw new UserValidationFailException(email);

        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    public void deleteUserLogic(UUID userId) {

        User user = userRepository.findById(userId).orElseThrow(()-> new UserNotExistException(userId));
        if(user.getDeletedAt()!=null) throw new UserNotExistException(userId);
        user.deleteLogic();
        return;
    }

    @Override
    public UserDto updateUser(UUID userId, UserUpdateRequest request) {
        String newNickName = request.nickname();
        User user = userRepository.findById(userId).orElseThrow(()-> new UserNotExistException(userId));
        if(user.getDeletedAt()!=null) throw new UserNotExistException(userId);
        user.updateNickName(newNickName);
        return userMapper.toDto(user);
    }

    @Override
    public void deleteUserHard(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(()-> new UserNotExistException(userId));
        userRepository.deleteById(userId);
    }


}
