package com.example.monew.domain.user.dto;

import jakarta.validation.Valid;

@Valid
public record UserLoginRequest(

        String email,
        String password
) {
}
