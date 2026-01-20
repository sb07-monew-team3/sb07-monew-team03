package com.example.monew.global.util.batch.tasklet;

import com.example.monew.domain.article.storage.S3ArticleStorage;
import com.example.monew.domain.user.service.UserDeleteScheduler;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ArticleBackupTasklet implements Tasklet {

    private final S3ArticleStorage s3ArticleStorage;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
       s3ArticleStorage.backupArticles();
        return RepeatStatus.FINISHED;
    }
}
