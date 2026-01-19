package com.example.monew.domain.article.controller;

import com.example.monew.domain.article.dto.*;
import com.example.monew.domain.article.service.ArticleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/articles")
public class ArticleController {

    private final ArticleService articleService;

    @GetMapping
    public ResponseEntity<CursorPageResponseArticleDto> getArticles(
            @RequestHeader(value = "Monew-Request-User-ID") UUID userId,
            @Valid ArticleRequestDto request
    ) {
        CursorPageResponseArticleDto response = articleService.getArticleList(request, userId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{articleId}")
    public ResponseEntity<ArticleDto> getArticle(
            @RequestHeader(value = "Monew-Request-User-ID") UUID userId,
            @PathVariable UUID articleId
    ) {
        ArticleDto response = articleService.getArticle(articleId, userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{articleId}")
    public ResponseEntity<Void> deleteArticleSoft(
            @PathVariable UUID articleId
    ) {
        articleService.deleteArticleSoft(articleId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{articleId}/hard")
    public ResponseEntity<Void> deleteArticleHard(
            @PathVariable UUID articleId
    ) {
       articleService.deleteArticleHard(articleId);
       return ResponseEntity.noContent().build();
    }

    @PostMapping("/{articleId}/article-views")
    public ResponseEntity<ArticleViewDto> recordArticleView(
            @RequestHeader(value = "Monew-Request-User-ID") UUID userId,
            @PathVariable UUID articleId
    ) {
        ArticleViewDto response = articleService.recordArticleView(articleId, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/sources")
    public ResponseEntity<List<String>> getSources() {
        List<String> sources = new ArrayList<>();

        for (Source source : Source.values()) {
            sources.add(source.getValue());
        }

        return ResponseEntity.ok(sources);
    }

    @GetMapping("/restore")
    public ResponseEntity<ArticleRestoreResultDto> restoreArticles(
            @RequestParam LocalDateTime from,
            @RequestParam LocalDateTime to
    ) {
        ArticleRestoreResultDto response = articleService.restoreArticles(from, to);

        return ResponseEntity.ok(response);
    }
}
