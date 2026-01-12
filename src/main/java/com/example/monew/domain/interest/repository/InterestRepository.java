package com.example.monew.domain.interest.repository;

import com.example.monew.domain.interest.entity.Interest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InterestRepository extends JpaRepository<Interest, UUID> {

    Optional<Interest> findByName(String name);

    boolean existsByName(String name);

    @Query("SELECT i.name FROM Interest i")
    List<String> findAllNames();

    @Query("SELECT i FROM Interest i " +
            "LEFT JOIN Keyword k ON k.interest = i " +
            "WHERE i.name LIKE %:keyword% OR k.keyword LIKE %:keyword%")
    List<Interest> searchByInterestOrKeyword(@Param("keyword") String keyword);
}
