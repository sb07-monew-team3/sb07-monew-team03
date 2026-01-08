package com.example.monew.domain.user.service;

import com.example.monew.domain.user.entity.User;
import com.example.monew.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDeleteScheduler {

    static final long BATCH_INTERVAL = 1000 * 60 * 60; //60분마다 호출함

    private final UserRepository userRepository;

    @Scheduled(fixedRate = BATCH_INTERVAL)
    public void deleteUser(){

        List<User> logicDeleteUser = userRepository.findLogicDeleteUser();
        if(logicDeleteUser.isEmpty()) return;
        userRepository.deleteAll(logicDeleteUser);

    }
}
