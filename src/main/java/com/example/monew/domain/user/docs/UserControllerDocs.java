package com.example.monew.domain.user.docs;

import com.example.monew.domain.user.dto.UserDto;
import com.example.monew.domain.user.dto.UserLoginRequest;
import com.example.monew.domain.user.dto.UserRegisterRequest;
import com.example.monew.domain.user.dto.UserUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "사용자 관리" ,description = "User API")
public interface UserControllerDocs {
    @Operation(summary = "유저 등록")
    @ApiResponse(
            responseCode = "200",
            description = "유저 생성 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserDto.class),
                    examples = @ExampleObject(
                            name = "유저 등록 성공",
                            value = """
                                    {
                                    "id" : "11111111111111111111111111111",
                                    "email" : "test123@naver.com",
                                    "nickname" : "test1234",
                                    "createdAt" : "2024-04-05"
                                    }
                                    """
                    )
            )
    )
    @PostMapping()
    ResponseEntity<UserDto> registerUser(@Valid @RequestBody UserRegisterRequest request);

    @Operation(summary = "유저 로그인")
    @ApiResponse(
            responseCode = "200",
            description = "유저 로그인 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserDto.class),
                    examples = @ExampleObject(
                            name = "유저 로그인 성공",
                            value = """
                                    {
                                    "id" : "11111111111111111111111111111",
                                    "email" : "test123@naver.com",
                                    "nickname" : "test1234",
                                    "createdAt" : "2024-04-05"
                                    }
                                    """
                    )
            )
    )
    @PostMapping("/login")
    ResponseEntity<UserDto> loginUser(@Valid @RequestBody UserLoginRequest request);

    @Operation(summary = "유저 논리 삭제")
    @ApiResponse(
            responseCode = "200",
            description = "유저 논리 삭제 성공",
            content = @Content(
                    mediaType = "application/json"
            )
    )
    @DeleteMapping("/{userId}")
    ResponseEntity<?> deleteUserLogic(@PathVariable("userId") UUID userId);

    @Operation(summary = "유저 업데이트")
    @ApiResponse(
            responseCode = "200",
            description = "유저 업데이트 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserDto.class),
                    examples = @ExampleObject(
                            name ="유저 업데이트 성공",
                            value = """
                                    {
                                    "id" : "11111111111111111111111111111",
                                    "email" : "test123@naver.com",
                                    "nickname" : "test1234",
                                    "createdAt" : "2024-04-05"
                                    }
                                    """
                    )
            )
    )
    @PatchMapping("/{userId}")
    ResponseEntity<UserDto> updateUser(@Valid @RequestBody UserUpdateRequest request, @PathVariable("userId") UUID userId);

    @Operation(summary = "유저 물리 삭제")
    @ApiResponse(
            responseCode = "200",
            description = "유저 물리 삭제 성공",
            content = @Content(
                    mediaType = "application/json"
            )
    )
    @DeleteMapping("/{userId}/hard")
    ResponseEntity<?> deleteUserHard(@PathVariable("userId") UUID userId);
}
