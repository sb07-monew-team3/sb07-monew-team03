package com.example.monew.domain.user.unit.controller;

import com.example.monew.domain.user.controller.UserController;
import com.example.monew.domain.user.dto.UserDto;
import com.example.monew.domain.user.dto.UserLoginRequest;
import com.example.monew.domain.user.dto.UserRegisterRequest;
import com.example.monew.domain.user.dto.UserUpdateRequest;
import com.example.monew.domain.user.service.UserService;
import com.example.monew.domain.user.util.TestFixture;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@WebMvcTest(UserController.class)
@DisplayName( "UserController Slice Test")
public class UserControllerSliceTest {


    private static final Logger log = LoggerFactory.getLogger(UserControllerSliceTest.class);
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new ParameterNamesModule());
    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    UserService userService;

    private UserDto userDto;
    private final TestFixture testFixture = new TestFixture();
    @BeforeEach
    void setUp(){
         userDto = new UserDto(UUID.randomUUID(),"seozo001@naver.com","hwempire",Instant.now());
    }

    @Test
    @DisplayName("[정상 케이스] 유저 등록 요청 성공")
    void createUser_validUserRegisterRequest_success() throws Exception {
        UserRegisterRequest request = testFixture.userRegisterRequestFactory();

        BDDMockito.given(userService.createUser(any(UserRegisterRequest.class))).willReturn(userDto);

        mockMvc.perform(multipart("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ). andExpect(jsonPath("$.email").value("seozo001@naver.com"))
                .andDo(result -> log.info(result.getResponse().getContentAsString()));

        ArgumentCaptor<UserRegisterRequest> requestArg = ArgumentCaptor.forClass(UserRegisterRequest.class);
        then(userService).should(times(1)).createUser(requestArg.capture());
        assertThat(requestArg.getValue().email()).isEqualTo(request.email());
    }

    @Test
    @DisplayName("[정상 케이스] 유저 로그인 요청 성공")
    void loginUser_validUserLoginRequest_success() throws Exception {

        BDDMockito.given(userService.loginUser(any(UserLoginRequest.class))).willReturn(userDto);
        UserLoginRequest request = new UserLoginRequest(
                "ronaldo@naver.com",
                "Test@1234"
        );
        mockMvc.perform(multipart("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                ). andExpect(jsonPath("$.email").value("seozo001@naver.com"))
                .andDo(result -> log.info(result.getResponse().getContentAsString()));

        ArgumentCaptor<UserLoginRequest> requestArg = ArgumentCaptor.forClass(UserLoginRequest.class);
        then(userService).should(times(1)).loginUser(requestArg.capture());
        assertThat(requestArg.getValue().email()).isEqualTo(request.email());
    }

    @Test
    @DisplayName("[정상 케이스 ] 유저 논리 삭제")
    void deleteUserLogic_validUser_success() throws Exception {
        willDoNothing().given(userService).deleteUserLogic(any(UUID.class));

        mockMvc.perform(delete("/api/users/"+userDto.id().toString())
                ). andExpect(status().isOk())
                .andDo(result -> log.info(result.getResponse().getContentAsString()));

        ArgumentCaptor<UUID> idArgumentCaptor = ArgumentCaptor.forClass(UUID.class);
        then(userService).should(times(1)).deleteUserLogic(idArgumentCaptor.capture());

        assertThat(idArgumentCaptor.getValue()).isEqualTo(userDto.id());

    }

    @Test
    @DisplayName("[정상 케이스] 유저 물리 삭제")
    void deleteUserHard_validUser_success() throws Exception {
        willDoNothing().given(userService).deleteUserHard(any(UUID.class));

        mockMvc.perform(delete("/api/users/"+userDto.id().toString()+"/hard")
                ). andExpect(status().isOk())
                .andDo(result -> log.info(result.getResponse().getContentAsString()));

        ArgumentCaptor<UUID> idArgumentCaptor = ArgumentCaptor.forClass(UUID.class);
        then(userService).should(times(1)).deleteUserHard(idArgumentCaptor.capture());

        assertThat(idArgumentCaptor.getValue()).isEqualTo(userDto.id());
    }

    @Test
    @DisplayName("[정상 케이스] 유저 정보 수정")
    void updateUser_validUser_success() throws Exception {
        UserUpdateRequest request = new UserUpdateRequest("hwempire");
        UUID expectedId = UUID.randomUUID();
        BDDMockito.given(userService.updateUser(
                any(UUID.class),
                any(UserUpdateRequest.class)
        )).willReturn(userDto);

        mockMvc.perform(patch("/api/users/" + expectedId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                ). andExpect(jsonPath("$.email").value("seozo001@naver.com"))
                .andDo(result -> log.info(result.getResponse().getContentAsString()));

        ArgumentCaptor<UserUpdateRequest> userUpdateArg = ArgumentCaptor.forClass(UserUpdateRequest.class);
        then(userService).should(times(1)).updateUser(eq(expectedId), userUpdateArg.capture());
        assertThat(userUpdateArg.getValue().nickname()).isEqualTo(request.nickname());

    }


}
