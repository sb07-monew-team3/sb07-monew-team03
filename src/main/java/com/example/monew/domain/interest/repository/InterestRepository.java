package com.example.monew.domain.interest.repository;

import com.example.monew.domain.interest.entity.Interest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface InterestRepository extends JpaRepository<Interest, UUID> {

    Optional<Interest> findByName(String name);
    boolean existsByName(String name);
}
