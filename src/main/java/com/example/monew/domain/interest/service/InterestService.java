package com.example.monew.domain.interest.service;

import com.example.monew.domain.interest.dto.InterestDto;
import com.example.monew.domain.interest.dto.InterestRegisterRequest;
import com.example.monew.domain.interest.dto.InterestUpdateRequest;

import java.util.UUID;

public interface InterestService {

    InterestDto create(InterestRegisterRequest request);
    InterestDto update(UUID interestId, InterestUpdateRequest request);

}
