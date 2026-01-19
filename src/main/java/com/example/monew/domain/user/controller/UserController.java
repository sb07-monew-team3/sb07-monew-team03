package com.example.monew.domain.user.controller;

import com.example.monew.domain.user.docs.UserControllerDocs;
import com.example.monew.domain.user.dto.UserDto;
import com.example.monew.domain.user.dto.UserLoginRequest;
import com.example.monew.domain.user.dto.UserRegisterRequest;
import com.example.monew.domain.user.dto.UserUpdateRequest;
import com.example.monew.domain.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController implements UserControllerDocs {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    @PostMapping()
    public ResponseEntity<UserDto> registerUser(@Valid @RequestBody UserRegisterRequest request){
        UserDto user = userService.createUser(request);
        return new ResponseEntity<UserDto>(user, HttpStatus.CREATED);
    }
    @PostMapping("/login")
    public ResponseEntity<UserDto> loginUser(@Valid @RequestBody UserLoginRequest request){

        UserDto user = userService.loginUser(request);
        return new ResponseEntity<UserDto>(user,HttpStatus.OK);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUserLogic(@PathVariable("userId") UUID userId){
        userService.deleteUserLogic(userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
    @PatchMapping("/{userId}")
    public ResponseEntity<UserDto> updateUser(@Valid @RequestBody UserUpdateRequest request, @PathVariable("userId") UUID userId){

        UserDto user = userService.updateUser(userId, request);
        return new ResponseEntity<UserDto>(user,HttpStatus.OK);
    }

    @DeleteMapping("/{userId}/hard")
    public ResponseEntity<?> deleteUserHard(@PathVariable("userId") UUID userId){

        userService.deleteUserHard(userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
