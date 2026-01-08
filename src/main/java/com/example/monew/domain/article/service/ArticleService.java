package com.example.monew.domain.article.service;

import com.example.monew.domain.article.entity.Article;
import com.example.monew.domain.article.repository.ArticleRepository;
import com.example.monew.global.exception.domain.article.ArticleNotExistException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;

    public void deleteArticleSoft(UUID articleId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ArticleNotExistException(articleId));

        article.deleteLogic();
        articleRepository.save(article);

        articleRepository.deleteById(articleId);
    }

}
