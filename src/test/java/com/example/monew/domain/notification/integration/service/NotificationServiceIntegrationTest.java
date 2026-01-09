package com.example.monew.domain.notification.integration.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.monew.domain.notification.entity.Notifications;
import com.example.monew.domain.notification.repository.NotificationRepository;
import com.example.monew.domain.notification.service.NotificationService;
import com.example.monew.domain.user.entity.User;
import com.example.monew.domain.user.repository.UserRepository;
import com.example.monew.domain.user.util.NotiFactory;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

//@Slf4j
@SpringBootTest
@Transactional
public class NotificationServiceIntegrationTest {

    @Autowired
    NotificationService notiService;
    @Autowired
    NotificationRepository notiRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    EntityManager em;
    @Autowired
    NotiFactory notiFactory;

    @Test
    @DisplayName("case ⭕️ - 배치 처리 확인 > 확인한 알림 중 1주일이 경과된 알림은 자동으로 삭제됩니다")
    void deleteNotificationInBatch() {
        //given
        User user = notiFactory.newUser();
        userRepository.save(user);

        notiFactory.newNoti(user, "💌noti 1", 3, false);  // 미확인 + 3일 경과 - ❌
        notiFactory.newNoti(user, "💌noti 2", 7, false);  // 미확인 + 1주일 경과 - ❌
        notiFactory.newNoti(user, "💌noti 3", 3, true);   // 확인 + 3일 경과 - ❌
        notiFactory.newNoti(user, "💌noti 4", 7, true);   // 확인 + 1주일 경과  -  ⭕️
        notiFactory.newNoti(user, "💌noti 5", 7, true);   // 확인 + 1주일 경과  -  ⭕️

        //when
        notiService.deleteNotificationInBatch();

        em.flush();
        em.clear();

        //then
        List<Notifications> results = notiRepository.findAllByUserId(user.getId())
            .stream()
            .peek(noti -> System.out.println("✅ 배치 삭제 후 남은 노티 : " + noti.toString()))
            .toList();

        assertThat(results).hasSize(3);
        assertThat(results.get(0).getContent()).isEqualTo("💌noti 1");
        assertThat(results.get(1).getContent()).isEqualTo("💌noti 2");
        assertThat(results.get(2).getContent()).isEqualTo("💌noti 3");
    }
}
