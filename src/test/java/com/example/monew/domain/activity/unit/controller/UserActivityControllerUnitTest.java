package com.example.monew.domain.activity.unit.controller;

import com.example.monew.domain.activity.controller.UserActivityController;
import com.example.monew.domain.activity.dto.UserActivityDto;
import com.example.monew.domain.activity.service.IMongoDbService;
import com.example.monew.domain.user.util.TestFixture;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserActivityController.class)
@DisplayName( "User Activity Controller Unit Test")
public class UserActivityControllerUnitTest {

    private static final Logger log = LoggerFactory.getLogger(UserActivityControllerUnitTest.class);
    @MockitoBean
    IMongoDbService mongoDbService;

    @Autowired
    MockMvc mockMvc;

    private final TestFixture testFixture = new TestFixture();

    @Test
    @DisplayName("[정상 케이스] 유저 활동 내역 조회 성공")
    void getUserActivity_validUserActivity_success() throws Exception {
        UUID expectedId = UUID.randomUUID();
        UserActivityDto userActivityDto = testFixture.userActivityDtoFactory();
        given(mongoDbService.getUserActivity(eq(expectedId)))
                .willReturn(userActivityDto);

        mockMvc.perform(get("/api/user-activities/"+ expectedId.toString()))
                .andExpect(status().isOk())
                .andDo(result -> log.info(result.getResponse().getContentAsString()));

        ArgumentCaptor<UUID> idArgumentCaptor = ArgumentCaptor.forClass(UUID.class);
        then(mongoDbService).should(times(1)).getUserActivity(idArgumentCaptor.capture());
        assertThat(idArgumentCaptor.getValue()).isEqualTo(expectedId);

    }

}
