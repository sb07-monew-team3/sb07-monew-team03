package com.example.monew.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDeleteScheduler {

    static final long BATCH_INTERVAL = 1000 * 60 * 60; //60분마다 호출함

    @Scheduled(fixedRate = BATCH_INTERVAL)
    public void deleteUser(){


    }
}
