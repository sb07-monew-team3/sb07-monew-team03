package com.example.monew.global.util.batch.scheduler;

import com.example.monew.global.exception.domain.batch.BatchJobFailException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchJobScheduler {

    private final JobLauncher jobLauncher;
    private final JobRegistry jobRegistry;
    private final JobExplorer jobExplorer;

    public void run(String jobName) {
        if(isRunning(jobName)) {
            log.warn("{} is already running. skip.", jobName);
            return;
        }
        try {
            Job job = jobRegistry.getJob(jobName);
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("runAt", System.currentTimeMillis())
                    .toJobParameters();
            jobLauncher.run(job, jobParameters);
        } catch (Exception e) {
            throw new BatchJobFailException(e.getMessage());
        }
        return;
    }

    public boolean isRunning(String jobName) {
        return !jobExplorer.findRunningJobExecutions(jobName).isEmpty();
    }


}