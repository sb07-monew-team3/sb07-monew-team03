package com.example.monew.domain.activity.service;

import com.example.monew.domain.activity.dto.UserActivityDto;

import java.util.UUID;

public interface UserActivityService {

    UserActivityDto getUserActivity(UUID userId);
}
