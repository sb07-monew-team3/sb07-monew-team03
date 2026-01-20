package com.example.monew.domain.article.repository;

import com.example.monew.domain.article.dto.ArticleDto;
import com.example.monew.domain.article.dto.ArticleQueryDto;
import com.example.monew.domain.article.dto.ArticleRequestDto;
import com.example.monew.domain.article.entity.Article;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface ArticleRepository extends JpaRepository<Article, UUID>, ArticleRepositoryCustom {

    List<Article> findAllBySourceUrlIn(Set<String> sourceUrls);

    Slice<ArticleQueryDto> findArticleSlice(ArticleRequestDto request, UUID userId, List<String> keywords, Pageable pageable);

    long countArticleSlice(ArticleRequestDto request, List<String> keywords);

    List<Article> findAllByPublishDateBetween(LocalDateTime start, LocalDateTime end);

    Optional<Article> findBySourceUrl(String sourceUrl);
}
