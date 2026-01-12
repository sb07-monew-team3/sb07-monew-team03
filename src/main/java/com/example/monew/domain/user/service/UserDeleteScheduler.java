package com.example.monew.domain.user.service;

import com.example.monew.domain.user.entity.User;
import com.example.monew.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDeleteScheduler {

    static final long BATCH_INTERVAL = 1000 * 60 * 60; //60분마다 호출함

    private final UserRepository userRepository;

    @Scheduled(fixedRate = BATCH_INTERVAL)
    @Transactional
    public void deleteUser(){
        Instant currentTime = Instant.now();
        List<User> logicDeleteUser = userRepository.findLogicDeleteUser();
        if(logicDeleteUser.isEmpty()) return;

        List<User> physicsDeleteUser = logicDeleteUser.stream().filter(
                x -> x.getDeletedAt().isBefore(currentTime.minus(24, ChronoUnit.HOURS))
        ).toList();
        userRepository.deleteAll(physicsDeleteUser);
    }
}
