package com.example.monew.domain.activity.integration.service;

import com.example.monew.domain.activity.service.UserActivityService;
import com.example.monew.domain.user.util.TestFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@DisplayName("User Activity Service Integration Test")
@Transactional
public class UserActivityServiceIntegrationTest {

    @Autowired
    private UserActivityService userActivityService;

    @Autowired
    private TestFixture testFixture;

    @Test
    @DisplayName("[정상 케이스] 유저 활동 내역 조회")
    void getUserActivity_validUser_success(){


    }



}
