package com.example.monew.domain.user.unit.service;


import com.example.monew.domain.user.entity.User;
import com.example.monew.domain.user.repository.UserRepository;
import com.example.monew.domain.user.service.UserDeleteScheduler;
import com.example.monew.domain.user.service.UserService;
import com.example.monew.domain.user.util.TestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserDeleteScheduler Unit Test")
public class UserDeleteSchedulerUnitTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDeleteScheduler userDeleteScheduler;


    private User user1;
    private User user2;
    private User user3;
    private final TestFixture testFixture = new TestFixture();

    @BeforeEach
    void setUp() {
        user1= testFixture.userFactory();
        user2= testFixture.userFactory();
        user3= testFixture.userFactory();
    }

    @Test
    @DisplayName("[정상 케이스 ] 논리 삭제된 유저 전체 삭제")
    void deleteUser_logicDeletedUser_success(){
        //given

        user1.deleteLogic();
        user2.deleteLogic();
        user3.deleteLogic();

        given(userRepository.findLogicDeleteUser()).willReturn(List.of(user1,user2,user3));
        willDoNothing().given(userRepository).deleteAll(any());
        //when
        userDeleteScheduler.deleteUser();
        //then
        then(userRepository).should(times(1)).findLogicDeleteUser();
        then(userRepository).should(times(1)).deleteAll(any());
    }

}
