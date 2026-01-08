package com.example.monew.domain.interest.service;

import com.example.monew.domain.interest.dto.InterestDto;
import com.example.monew.domain.interest.dto.InterestRegisterRequest;

public interface InterestService {

    InterestDto create(InterestRegisterRequest request);
}
