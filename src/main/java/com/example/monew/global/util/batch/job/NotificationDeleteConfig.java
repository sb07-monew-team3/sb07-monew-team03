package com.example.monew.global.util.batch.job;

import com.example.monew.global.util.batch.JobStatus;
import com.example.monew.global.util.batch.metrics.JobMetricsListener;
import com.example.monew.global.util.batch.tasklet.NotificationDeleteTasklet;
import com.example.monew.global.util.batch.tasklet.UserDeleteTasklet;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class NotificationDeleteConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final NotificationDeleteTasklet notificationDeleteTasklet;
    private final JobMetricsListener jobMetricsListener;

    @Bean
    public Job notificationDeleteJob(){
        return new JobBuilder(JobStatus.NOTIFICATION_DELETE.getJobName(), jobRepository)
                .start(notificationDeleteStep())
                .listener(jobMetricsListener)
                .build();
    }

    @Bean
    public Step notificationDeleteStep(){
        return new StepBuilder(JobStatus.NOTIFICATION_DELETE.getStepName(), jobRepository)
                .tasklet(notificationDeleteTasklet,transactionManager)
                .build();
    }
}
