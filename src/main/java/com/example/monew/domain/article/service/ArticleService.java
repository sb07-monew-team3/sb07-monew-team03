package com.example.monew.domain.article.service;

import com.example.monew.domain.article.dto.ArticleDto;
import com.example.monew.domain.article.entity.Article;
import com.example.monew.domain.article.mapper.ArticleMapper;
import com.example.monew.domain.article.repository.ArticleRepository;
import com.example.monew.global.exception.domain.article.ArticleNotExistException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;

    private final ArticleMapper articleMapper;

    public void deleteArticleSoft(UUID articleId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ArticleNotExistException(articleId));

        article.deleteLogic();
        articleRepository.save(article);
    }

    public void deleteArticleHard(UUID articleId) {
        articleRepository.findById(articleId)
                .orElseThrow(() -> new ArticleNotExistException(articleId));

        articleRepository.deleteById(articleId);
    }

    public ArticleDto getArticle(UUID articleId, UUID userId) {

        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ArticleNotExistException(articleId));

        return articleMapper.toDto(article, userId);
    }
}
