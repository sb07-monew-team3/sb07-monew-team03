package com.example.monew.domain.article.repository;

import com.example.monew.domain.article.entity.ArticleView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ArticleViewRepository extends JpaRepository<ArticleView, UUID>{

    boolean existsByArticleIdAndUserId(UUID articleId, UUID userId);

    int countByArticleId(UUID articleId);

    List<ArticleView> findAllByUserId(UUID userId);
}
