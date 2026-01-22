package com.example.monew.global.util.batch.job;

import com.example.monew.global.util.batch.JobStatus;
import com.example.monew.global.util.batch.metrics.JobMetricsListener;
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
public class UserDeleteJobConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final UserDeleteTasklet userDeleteTasklet;
    private final JobMetricsListener jobMetricsListener;

    @Bean
    public Job userDeleteJob(){
        return new JobBuilder(JobStatus.USER_DELETE.getJobName(), jobRepository)
                .start(userDeleteStep())
                .listener(jobMetricsListener)
                .build();
    }

    @Bean
    public Step userDeleteStep(){
        return new StepBuilder(JobStatus.USER_DELETE.getStepName(), jobRepository)
                .tasklet(userDeleteTasklet,transactionManager)
                .build();
    }
}
