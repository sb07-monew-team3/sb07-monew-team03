package com.example.monew.domain.user.dto;

import jakarta.validation.Valid;

@Valid
public record UserRegisterRequest(

        String email,
        String nickname,
        String password
) {
}
