package com.example.monew.domain.user.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Valid
public record UserLoginRequest(

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email pattern")
        String email,

        @NotBlank(message = "Password is required")
        String password
) {
}
