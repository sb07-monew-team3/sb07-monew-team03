package com.example.monew.domain.activity.controller;


import com.example.monew.domain.activity.docs.UserActivityControllerDocs;
import com.example.monew.domain.activity.dto.UserActivityDto;
import com.example.monew.domain.activity.service.MongoDbService;
import com.example.monew.domain.activity.service.UserActivityService;
import com.example.monew.global.config.MongoConfig;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
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
public class UserActivityController implements UserActivityControllerDocs {

    private final MongoDbService mongoDbService;

    @GetMapping("/{userId}")
    public ResponseEntity<UserActivityDto> getUserActivity(@PathVariable("userId") UUID userId){

        UserActivityDto userActivity = mongoDbService.getUserActivity(userId);
        return new ResponseEntity<>(userActivity, HttpStatus.OK);
    }
}
