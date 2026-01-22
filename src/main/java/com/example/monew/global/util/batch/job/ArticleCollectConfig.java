package com.example.monew.global.util.batch.job;

import com.example.monew.global.util.batch.JobStatus;
import com.example.monew.global.util.batch.metrics.JobMetricsListener;
import com.example.monew.global.util.batch.tasklet.ArticleCollectTasklet;
import com.example.monew.global.util.batch.tasklet.NotificationDeleteTasklet;
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
public class ArticleCollectConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final ArticleCollectTasklet articleCollectTasklet;
    private final JobMetricsListener jobMetricsListener;

    @Bean
    public Job articleCollectJob(){
        return new JobBuilder(JobStatus.ARTICLE_COLLECT.getJobName(), jobRepository)
                .start(articleCollectStep())
                .listener(jobMetricsListener)
                .build();
    }

    @Bean
    public Step articleCollectStep(){
        return new StepBuilder(JobStatus.ARTICLE_COLLECT.getStepName(), jobRepository)
                .tasklet(articleCollectTasklet,transactionManager)
                .build();
    }
}
