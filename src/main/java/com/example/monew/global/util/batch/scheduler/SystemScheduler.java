package com.example.monew.global.util.batch.scheduler;

import com.example.monew.global.util.batch.JobStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SystemScheduler {

    private final BatchJobScheduler batchJobScheduler;
    static final long BATCH_INTERVAL_DAY = 1000 * 60 * 60 * 24;
    static final long BATCH_INTERVAL_HOUR = 1000* 60 * 60 ;

    @Scheduled(fixedRate = BATCH_INTERVAL_DAY)
    public void runUserDeleteJob(){
        batchJobScheduler.run(JobStatus.USER_DELETE.getJobName());
    }

    @Scheduled(fixedRate = BATCH_INTERVAL_DAY)
    public void runNotificationDeleteJob(){
        batchJobScheduler.run(JobStatus.NOTIFICATION_DELETE.getJobName());
    }

    @Scheduled(fixedRate = BATCH_INTERVAL_HOUR)
    public void runArticleCollectJob(){
        batchJobScheduler.run(JobStatus.ARTICLE_COLLECT.getJobName());
    }
}
