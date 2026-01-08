package com.example.monew.domain.user.repository;

import com.example.monew.domain.user.entity.User;

import java.util.List;

public interface UserRepositoryCustom {

    boolean isEmailExist(String email);
    List<User> findLogicDeleteUser();
}
