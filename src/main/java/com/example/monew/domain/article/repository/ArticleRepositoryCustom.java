package com.example.monew.domain.article.repository;

import com.example.monew.domain.article.dto.ArticleDto;
import com.example.monew.domain.article.dto.ArticleRequestDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;
import java.util.UUID;

public interface ArticleRepositoryCustom {
    Slice<ArticleDto> findArticleSlice(ArticleRequestDto request, UUID userId, List<String> keywords, Pageable pageable);

    long countArticleSlice(ArticleRequestDto request, List<String> keywords);
}
