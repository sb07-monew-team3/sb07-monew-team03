package com.example.monew.domain.interest.service;

import com.example.monew.domain.interest.dto.CursorPageResponseInterestDto;
import com.example.monew.domain.interest.dto.InterestDto;
import com.example.monew.domain.interest.dto.InterestRegisterRequest;
import com.example.monew.domain.interest.dto.InterestUpdateRequest;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface InterestService {

    InterestDto create(InterestRegisterRequest request);
    InterestDto update(UUID interestId, InterestUpdateRequest request);
    void delete(UUID interestId);
    CursorPageResponseInterestDto search(
            String keyword,
            UUID userId,
            String orderBy,
            String direction,
            String cursor,
            Instant after,
            int limit);
}
