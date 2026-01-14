package com.example.monew.domain.interest.unit.controller;

import com.example.monew.domain.interest.controller.InterestController;
import com.example.monew.domain.interest.dto.*;
import com.example.monew.domain.interest.entity.Interest;
import com.example.monew.domain.interest.service.InterestService;
import com.example.monew.domain.interest.service.SubscriptionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InterestController.class)
public class InterestControllerTest {

    @Autowired
    private MockMvc mockmvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private InterestService interestService;

    @MockitoBean
    private SubscriptionService subscriptionService;


    @Test
    @DisplayName("관심사 등록 성공 시 201 Created를 반환한다")
    void registerInterest_success() throws Exception {

        // given
        Interest interest = new Interest("동물");
        ReflectionTestUtils.setField(interest, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(interest, "createdAt", Instant.now());

        InterestRegisterRequest request = new InterestRegisterRequest(
                "동물",
                List.of("강아지", "고양이")
        );

        InterestDto interestDto = new InterestDto(
                interest.getId(),
                interest.getName(),
                request.keywords(),
                0L,
                false
        );

        when(interestService.create(any())).thenReturn(interestDto);

        // when
        mockmvc.perform(post("/api/interests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(interestDto.id().toString()))
                .andExpect(jsonPath("$.name").value(interestDto.name()))
                .andExpect(jsonPath("$.keywords", hasSize(2)));

        // then
        verify(interestService).create(any());
    }

    @Test
    @DisplayName("키워드 수정 성공 시 200 OK를 반환한다")
    void updateInterest_success() throws Exception {

        // given
        Interest interest = new Interest("동물");
        ReflectionTestUtils.setField(interest, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(interest, "createdAt", Instant.now());

        InterestUpdateRequest request = new InterestUpdateRequest(
                List.of("강아지", "고양이", "사자")
        );

        InterestDto interestDto = new InterestDto(
                interest.getId(),
                interest.getName(),
                request.keywords(),
                0L,
                false
        );

        when(interestService.update(any(UUID.class), any(InterestUpdateRequest.class)))
                .thenReturn(interestDto);

        // when
        mockmvc.perform(patch("/api/interests/{interestId}", interest.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.keywords", hasSize(3)));


        // then
        verify(interestService).update(any(UUID.class), any(InterestUpdateRequest.class));
    }

    @Test
    @DisplayName("관심사 삭제 성공 시 204 No Content를 반환한다")
    void deleteInterest_success() throws Exception {

        // given
        UUID interestId = UUID.randomUUID();

        doNothing().when(interestService).delete(interestId);

        // when
        mockmvc.perform(delete("/api/interests/{interestId}", interestId))
                .andExpect(status().isNoContent());

        // then
        verify(interestService).delete(interestId);
    }
    
    @Test
    @DisplayName("관심사 구독 성공 시 200 OK를 반환한다")
    void subscribe_success() throws Exception {

        // given
        UUID subscribeId = UUID.randomUUID();
        UUID interestId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        SubscriptionDto dto = new SubscriptionDto(
                subscribeId,
                interestId,
                "동물",
                List.of("강아지", "고양이"),
                0L,
                Instant.now()
        );

        when(subscriptionService.subscribe(any(UUID.class), any(UUID.class))).thenReturn(dto);
        
        // when
        mockmvc.perform(post("/api/interests/{interestId}/subscriptions", interestId)
                        .header("Monew-Request-User-ID", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(subscribeId.toString()))
                .andExpect(jsonPath("$.interestId").value(interestId.toString()));

        // then
        verify(subscriptionService).subscribe(interestId, userId);
    }

    @Test
    @DisplayName("관심사 구독 취소 성공 시 204 No Content를 반환한다")
    void unsubscribe_success() throws Exception {

        // given
        UUID interestId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        doNothing().when(subscriptionService).unsubscribe(interestId, userId);

        // when
        mockmvc.perform(delete("/api/interests/{interestId}/subscriptions", interestId)
                        .header("Monew-Request-User-ID", userId.toString()))
                .andExpect(status().isNoContent());

        // then
        verify(subscriptionService).unsubscribe(interestId, userId);
    }

    @Test
    @DisplayName("관심사 검색 성공 시 커서 페이지 응답한다")
    void searchInterest_cursorPage_success() throws Exception {

        // given
        UUID userId = UUID.randomUUID();
        InterestDto interestDto = new InterestDto(
                UUID.randomUUID(),
                "동물",
                List.of("강아지", "고양이"),
                3L,
                true
        );

        CursorPageResponseInterestDto response = new CursorPageResponseInterestDto(
                List.of(interestDto),
                null,
                null,
                1,
                1L,
                false
        );

        when(interestService.search(anyString(), any(UUID.class), anyString(),
                anyString(), anyString(), any(Instant.class), anyInt()))
                .thenReturn(response);

        // when && then
        mockmvc.perform(get("/api/interests")
                        .param("keyword", "동물")
                        .param("orderBy", "create_at")
                        .param("direction", "desc")
                        .param("cursor", "")
                        .param("after", "")
                        .param("limit", "10")
                        .header("Monew-Request-User-ID", userId.toString()))
                .andExpect(status().isOk());
    }
}
