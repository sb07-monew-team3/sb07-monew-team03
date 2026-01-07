package com.example.monew.domain.user.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Valid
public record UserRegisterRequest(

        @NotBlank
        @Email(message= "Invalid email pattern")
        String email,

        @NotBlank
        String nickname,

        // 영문 대문자 ,영문 소문자,숫자,특수기호 최소 1개 이상, 크기는 8 이상
        @NotBlank
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+~`\\-={}\\[\\]:;\"'<>,.?/]).{8,}$")
        String password
) {
}

