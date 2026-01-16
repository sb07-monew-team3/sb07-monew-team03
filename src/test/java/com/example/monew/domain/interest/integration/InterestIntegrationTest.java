package com.example.monew.domain.interest.integration;

import com.example.monew.domain.interest.dto.InterestRegisterRequest;
import com.example.monew.domain.interest.dto.InterestUpdateRequest;
import com.example.monew.domain.interest.entity.Interest;
import com.example.monew.domain.interest.entity.Keyword;
import com.example.monew.domain.interest.entity.Subscription;
import com.example.monew.domain.interest.repository.InterestRepository;
import com.example.monew.domain.interest.repository.KeywordRepository;
import com.example.monew.domain.interest.repository.SubscriptionRepository;
import com.example.monew.domain.notification.repository.NotificationRepository;
import com.example.monew.domain.user.entity.User;
import com.example.monew.domain.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
public class InterestIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InterestRepository interestRepository;

    @Autowired
    private KeywordRepository keywordRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    private UUID userId;

    @BeforeEach
    void setUp() {

        userId = UUID.randomUUID();
    }

    @Test
    @DisplayName("관심사를 등록할 수 있다")
    void registerInterest_Success() throws Exception {

        // given
        InterestRegisterRequest request = new InterestRegisterRequest(
                "코딩",
                List.of("자바", "스프링", "JPA")
        );

        String requestJson = objectMapper.writeValueAsString(request);

        // when && then
        mockMvc.perform(post("/api/interests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("코딩"))
                .andExpect(jsonPath("$.keywords.length()").value(3));
    }
    
    @Test
    @DisplayName("동일한 관심사 등록 시 409 반환한다")
    void registerInterest_DuplicateName() throws Exception {

        // given
        Interest interest = new Interest("코딩");
        interestRepository.save(interest);

        InterestRegisterRequest request = new InterestRegisterRequest(
                "코딩",
                List.of("자바", "스프링", "JPA")
        );

        String requestJson = objectMapper.writeValueAsString(request);

        // when && then
        mockMvc.perform(post("/api/interests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("유사한 관심사 등록 시 409 반환한다")
    void registerInterest_SimilarName() throws Exception {

        // given
        Interest interest = new Interest("축구축구");
        interestRepository.save(interest);

        InterestRegisterRequest request = new InterestRegisterRequest(
                "축구축구@",
                List.of("운동", "손흥민", "인테르")
        );

        String requestJson = objectMapper.writeValueAsString(request);

        // when && then
        mockMvc.perform(post("/api/interests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isConflict());

    }

    @Test
    @DisplayName("관심사 키워드를 수정할 수 있다")
    void updateInterest_Success() throws Exception {

        // given
        Interest interest = new Interest("코딩");
        interestRepository.save(interest);

        List<Keyword> keywords = List.of(
                new Keyword("자바", interest),
                new Keyword("스프링", interest),
                new Keyword("JPA", interest)
        );

        keywordRepository.saveAll(keywords);

        InterestUpdateRequest request = new InterestUpdateRequest(
                List.of("파이썬", "리액트")
        );

        String requestJson = objectMapper.writeValueAsString(request);

        // when && then
        mockMvc.perform(patch("/api/interests/{interestId}", interest.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("코딩"))
                .andExpect(jsonPath("$.keywords.length()").value(2));
    }

    @Test
    @DisplayName("없는 관심사 키워드 수정 시 404 반환한다")
    void updateInterest_NotFound() throws Exception {

        // given
        UUID interestId = UUID.randomUUID();

        InterestUpdateRequest request = new InterestUpdateRequest(
                List.of("수정 키워드", "얌얌")
        );

        String requestJson = objectMapper.writeValueAsString(request);

        // when && then
        mockMvc.perform(patch("/api/interests/{interestId}", interestId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("INTEREST_NOT_EXIST"));
    }

    @Test
    @DisplayName("관심사를 삭제할 수 있다")
    void deleteInterest_Success() throws Exception {

        // given
        Interest interest = new Interest("코딩");
        interestRepository.save(interest);

        // when && then
        mockMvc.perform(delete("/api/interests/{interestId}", interest.getId()))
                .andExpect(status().isNoContent());

        Optional<Interest> deleted = interestRepository.findById(interest.getId());
        assertThat(deleted).isEmpty();
    }
    
    @Test
    @DisplayName("없는 관심사 삭제 시 404 반환한다")
    void deleteInterest_NotFound() throws Exception{

        // given
        UUID interestId = UUID.randomUUID();

        // when && then
        mockMvc.perform(delete("/api/interests/{interestId}", interestId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("INTEREST_NOT_EXIST"));
    }

    @Test
    @DisplayName("키워드로 관심사 검색할 수 있다")
    void searchInterest_Keyword_Success() throws Exception{

        // given
        Interest interest = new Interest("코딩");
        Interest interest2 = new Interest("스포츠");

        interestRepository.save(interest);
        interestRepository.save(interest2);

        List<Keyword> keywords = List.of(
                new Keyword("자바", interest),
                new Keyword("스프링", interest),
                new Keyword("JPA", interest)
        );

        List<Keyword> keywords2 = List.of(
                new Keyword("축구", interest2),
                new Keyword("야구", interest2),
                new Keyword("배구", interest2)
        );

        keywordRepository.saveAll(keywords);
        keywordRepository.saveAll(keywords2);


        // when && then
        mockMvc.perform(get("/api/interests?keyword=자바&orderBy=name&direction=asc&limit=10")
                .header("Monew-Request-User-ID", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("코딩"));
    }

    @Test
    @DisplayName("관심사 구독할 수 있다")
    void subscribeInterest_Success() throws Exception {

        // given
        User user = new User("test22@test.com", "테스트", "Z1x2c3v4!", null);
        userRepository.save(user);

        Interest interest = new Interest("코딩");
        interestRepository.save(interest);

        List<Keyword> keywords = List.of(
                new Keyword("자바", interest),
                new Keyword("스프링", interest),
                new Keyword("JPA", interest)
        );

        keywordRepository.saveAll(keywords);

        // when && then
        mockMvc.perform(post("/api/interests/{interestId}/subscriptions", interest.getId())
                .header("Monew-Request-User-ID", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.interestId").value(interest.getId().toString()))
                .andExpect(jsonPath("$.interestSubscriberCount").value(1));
    }
    
    @Test
    @DisplayName("없는 관심사 구독 시 404 반환한다")
    void subscribeInterest_NotFound() throws Exception {

        // given
        User user = new User("test22@test1.com", "테스트1", "Z1x2c3v4!", null);
        userRepository.save(user);

        UUID interestId = UUID.randomUUID();

        // when && then
        mockMvc.perform(post("/api/interests/{interestId}/subscriptions", interestId)
                .header("Monew-Request-User-ID", user.getId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("INTEREST_NOT_EXIST"));
    }
    
    @Test
    @DisplayName("관심사 구독 취소 할 수 있다")
    void unsubscribeInterest_Success() throws Exception {

        // given
        User user = new User("abcd@abc.com", "테스트1", "Z1x2c3v4!", null);
        userRepository.save(user);

        Interest interest = new Interest("코딩");
        interestRepository.save(interest);

        Subscription subscription = new Subscription(interest, user);
        subscriptionRepository.save(subscription);

        // when && then
        mockMvc.perform(delete("/api/interests/{interestId}/subscriptions", interest.getId())
                .header("Monew-Request-User-ID", user.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("없는 관심사 구독 취소 시 404 반환")
    void unsubscribeInterest_NotFound() throws Exception {

        // given
        User user = new User("e@e.com", "테스트2", "Z1x2c3v4!", null);
        userRepository.save(user);

        Interest interest = new Interest("코딩");
        interestRepository.save(interest);

        // when && then
        mockMvc.perform(delete("/api/interests/{interestId}/subscriptions", interest.getId())
                .header("Monew-Request-User-ID", user.getId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("SUBSCRIPTION_NOT_EXIST"));
    }
}