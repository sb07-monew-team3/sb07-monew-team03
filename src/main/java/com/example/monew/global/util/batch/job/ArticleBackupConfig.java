package com.example.monew.global.util.batch.job;

import com.example.monew.global.util.batch.JobStatus;
import com.example.monew.global.util.batch.tasklet.ArticleBackupTasklet;
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
public class ArticleBackupConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final ArticleBackupTasklet articleBackupTasklet;

    @Bean
    public Job articleBackupJob(){
        return new JobBuilder(JobStatus.ARTICLE_BACKUP.getJobName(), jobRepository)
                .start(articleDeleteStep())
                .build();
    }

    @Bean
    public Step articleDeleteStep(){
        return new StepBuilder(JobStatus.ARTICLE_BACKUP.getStepName(), jobRepository)
                .tasklet(articleBackupTasklet,transactionManager)
                .build();
    }
}
