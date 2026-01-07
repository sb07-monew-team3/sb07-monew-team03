package com.example.monew.domain.user.repository;

import com.example.monew.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;


public interface UserRepository extends JpaRepository<User, UUID> ,UserRepositoryCustom{


boolean isEmailExist(String email);
Optional<User> findByEmail(String email);
}
