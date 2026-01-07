package com.example.monew.domain.user.controller;

import com.example.monew.domain.user.dto.UserDto;
import com.example.monew.domain.user.dto.UserLoginRequest;
import com.example.monew.domain.user.dto.UserRegisterRequest;
import com.example.monew.domain.user.dto.UserUpdateRequest;
import com.example.monew.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping()
    ResponseEntity<UserDto> registerUser(@Valid @RequestBody UserRegisterRequest request){
        UserDto user = userService.createUser(request);
        return new ResponseEntity<UserDto>(user, HttpStatus.CREATED);
    }
    @PostMapping("/login")
    ResponseEntity<UserDto> loginUser(@Valid @RequestBody UserLoginRequest request){
        UserDto user = userService.loginUser(request);
        return new ResponseEntity<UserDto>(user,HttpStatus.OK);
    }

    @DeleteMapping("/{userId}")
    ResponseEntity<?> deleteUserLogic(@PathVariable("userId") UUID userId){
        userService.deleteUserLogic(userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
    @PatchMapping("/{userId}")
    ResponseEntity<UserDto> updateUser(@Valid @RequestBody UserUpdateRequest request, @PathVariable("userId") UUID userId){

        UserDto user = userService.updateUser(userId, request);
        return new ResponseEntity<UserDto>(user,HttpStatus.OK);
    }

    @DeleteMapping("/{userId}/hard")
    ResponseEntity<?> deleteUserHard(@PathVariable("userId") UUID userId){

        userService.deleteUserHard(userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
