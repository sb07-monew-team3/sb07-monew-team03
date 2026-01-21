package com.example.monew.global.util.batch.tasklet;

import com.example.monew.domain.activity.service.MongoDbService;
import com.example.monew.domain.user.entity.User;
import com.example.monew.domain.user.repository.UserRepository;
import com.example.monew.domain.user.service.UserDeleteScheduler;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
public class UserDeleteTasklet implements Tasklet {

    private final UserDeleteScheduler userDeleteScheduler;
    private final MongoDbService mongoDbService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
       userDeleteScheduler.deleteUser();
       mongoDbService.mongoConnection();
        return RepeatStatus.FINISHED;
    }
}
