package com.example.monew.global.util.batch.tasklet;

import com.example.monew.domain.article.service.ArticleCollectionScheduler;
import com.example.monew.domain.user.service.UserDeleteScheduler;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ArticleCollectTasklet implements Tasklet {

    private final ArticleCollectionScheduler articleCollectionScheduler;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
       articleCollectionScheduler.collectArticles();
        return RepeatStatus.FINISHED;
    }
}
