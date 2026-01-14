package com.example.monew.domain.notification.service;

import com.example.monew.domain.comment.service.CommentLikeService;
import com.example.monew.domain.interest.entity.Interest;
import com.example.monew.domain.interest.service.SubscriptionService;
import com.example.monew.domain.notification.dto.NotificationDto;
import com.example.monew.domain.notification.entity.Notifications;
import com.example.monew.domain.notification.entity.ResourceType;
import com.example.monew.domain.notification.repository.NotificationRepository;
import com.example.monew.domain.notification.response.CursorResponse;
import com.example.monew.global.exception.domain.notification.NotificationNotExistException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notiRepository;
    private final SubscriptionService subscriptionService;

    @Override
    public CursorResponse<NotificationDto> findAllByUserId(UUID userId, String cursor, Instant createdAt, int limit) {

        Pageable pageable = PageRequest.of(Integer.parseInt(Optional.ofNullable(cursor).orElse("0")), limit, Sort.by(Sort.Direction.DESC, "createdAt"));

        Slice<NotificationDto> sliceDto = notiRepository.findAllByUserId(userId, Optional.ofNullable(createdAt).orElse(Instant.now()), pageable)
            .map(NotificationDto::toDto);

        CursorResponse<NotificationDto> cursorResponses = NotificationDto.dtoCursorResponse(sliceDto);
        log.info(" findAllNotificationByUserId.cursorResponses = " + cursorResponses);

        return cursorResponses;
    }

    @Transactional
    @Override
    public void allCheckNotification(UUID userId) {
        notiRepository.findAllByUserId(userId)
            .stream()
            .peek(noti -> log.info("##### allCheckNotification.userId = " + userId.toString()))
            .forEach(noti -> noti.checkNotificationRead(userId));
    }

    @Transactional
    @Override
    public void checkNotification(UUID notificationId, UUID userId) {
        Notifications notifications = notiRepository.findByIdAndUserId(notificationId, userId)
            .orElseThrow(() -> new NotificationNotExistException(notificationId, userId));

        notifications.checkNotificationRead(userId);

        log.info("##### allCheckNotification.userId = " + userId.toString());
    }

    @Transactional
    @Override
    public void deleteNotificationInBatch() {
        // 확인한 알림 중 1주일이 경과된 알림은 자동으로 삭제됩니다.
        Instant oneWeekAgo = Instant.now().minus(7, ChronoUnit.DAYS);

        List<Notifications> notificationsList = notiRepository.findBatchDeleteNotification(oneWeekAgo)
            .stream()
            .peek(noti -> log.info("#####" + noti.toString()))
            .toList();

        if (!notificationsList.isEmpty()) {
            notiRepository.deleteAll(notificationsList);
            log.info("##### NotificationDeleteScheduler  - 노티 배치 삭제 완료");
        }
        else {
            log.info("##### NotificationDeleteScheduler  -  삭제할 노티 없음");
        }
    }

    // 내가 작성한 댓글에 좋아요가 눌리면 알림 생성
    @Override
    public void notifyCommentLiked(UUID userId, String actorNickname, UUID resouce_id) {
        String content = "[" + actorNickname + "]님이 나의 댓글을 좋아합니다.";

        Notifications notification = new Notifications(
            userId,
            content,
            ResourceType.COMMENT,
            resouce_id
        );

        notiRepository.save(notification);
    }

    @Override
    // 구독 중인 관심사와 관련된 기사가 새로 등록된 경우 알림 생성
    public void createInterestAlarm(Map<Interest, Integer> interestList) {

        for (Map.Entry<Interest, Integer> entry : interestList.entrySet()) {
            Interest interest = entry.getKey();
            String content = "[" + interest.getName() + "]와 관련된 기사가 " + entry.getValue().toString() + "건 등록되었습니다.";

            subscriptionService.getSubscribedInterestIds(interest.getId())
                .forEach(userId -> {

                    Notifications notification = new Notifications(
                        userId,
                        content,
                        ResourceType.INTEREST,
                        interest.getId()
                    );

                    notiRepository.save(notification);

                    log.info("##### createInterestAlarm.userId = {} : {}", userId.toString(), content);
                });
        }
    }
}
