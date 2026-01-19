package com.example.monew.domain.interest.repository;

import com.example.monew.domain.interest.entity.Interest;
import com.example.monew.domain.interest.entity.Keyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface KeywordRepository extends JpaRepository<Keyword, UUID>{

    List<Keyword> findAllByInterestId(UUID interestId);

    @Modifying
    @Query("DELETE FROM Keyword k WHERE k.interest.id = :interestId")
    void deleteByInterestId(UUID interestId);

    List<Keyword> findByInterest(Interest interest);
}
