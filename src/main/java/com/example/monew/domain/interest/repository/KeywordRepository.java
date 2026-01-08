package com.example.monew.domain.interest.repository;

import com.example.monew.domain.interest.entity.Keyword;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface KeywordRepository extends JpaRepository<Keyword, UUID>{

    List<Keyword> findAllByInterestId(UUID interestId);
}
