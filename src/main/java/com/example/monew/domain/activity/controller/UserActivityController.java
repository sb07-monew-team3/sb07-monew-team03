package com.example.monew.domain.activity.controller;


import com.example.monew.domain.activity.dto.UserActivityDto;
import com.example.monew.domain.activity.service.UserActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user-activities")
public class UserActivityController {

    private final UserActivityService userActivityService;

    @GetMapping("/{userId}")
    public ResponseEntity<UserActivityDto> getUserActivity(@PathVariable("userId") UUID userId){

        UserActivityDto userActivity = userActivityService.getUserActivity(userId);
        return new ResponseEntity<>(userActivity, HttpStatus.OK);
    }
}
