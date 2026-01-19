package com.example.monew.domain.interest.repository;

import com.example.monew.domain.interest.entity.Interest;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface InterestRepositoryCustom {

    List<Interest> searchByInterestOrKeyword(
            String keyword,
            String orderBy,
            String direction,
            String cursor,
            Instant after,
            int limit);
}
