package com.example.monew.global.util.batch.metrics;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.data.mongodb.util.DurationUtil;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class JobMetricsListener implements JobExecutionListener {

    private final BatchMetrics batchMetrics;

    @Override
    public void afterJob(JobExecution jobExecution) {
        String jobName = jobExecution.getJobInstance().getJobName();

        batchMetrics.incrementJobCount(jobName);

        LocalDateTime startTime = jobExecution.getStartTime();
        LocalDateTime endTime = jobExecution.getEndTime();

        long durationNano = Duration.between(startTime, endTime).getNano();

        batchMetrics.recordJobDuration(jobName, durationNano);
    }
}
