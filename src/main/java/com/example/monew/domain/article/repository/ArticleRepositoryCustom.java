package com.example.monew.domain.article.repository;

import com.example.monew.domain.article.dto.ArticleDto;
import com.example.monew.domain.article.dto.ArticleRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ArticleRepositoryCustom {
    Page<ArticleDto> findArticleSlice(ArticleRequestDto request, UUID userId, List<String> keywords, Pageable pageable);
}
