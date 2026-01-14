package com.example.monew.global.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@Configuration
@ConditionalOnProperty(value = "scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class SchedulerConfig {
}
