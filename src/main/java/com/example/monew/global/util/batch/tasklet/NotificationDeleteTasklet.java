package com.example.monew.global.util.batch.tasklet;

import com.example.monew.domain.notification.service.NotificationDeleteScheduler;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.Cascade;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationDeleteTasklet implements Tasklet {

    private final NotificationDeleteScheduler notificationDeleteScheduler;
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        notificationDeleteScheduler.deleteNotificationInBatch();
        return RepeatStatus.FINISHED;
    }
}
