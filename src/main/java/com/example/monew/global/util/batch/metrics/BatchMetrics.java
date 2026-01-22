package com.example.monew.global.util.batch.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class BatchMetrics {
    private final MeterRegistry meterRegistry;

    public void incrementJobCount(String jobName){
        meterRegistry.counter("batch.job.count", "jobName", jobName).increment();
    }
    public void recordJobDuration(String jobName, long duration){

        meterRegistry.timer("batch.job.duration","job",jobName).record(duration, TimeUnit.NANOSECONDS);

    }
}
