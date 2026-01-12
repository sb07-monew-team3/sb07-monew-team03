package com.example.monew.domain.article.mapper;

import com.example.monew.domain.article.dto.ArticleDto;
import com.example.monew.domain.article.dto.CursorPageResponseArticleDto;
import com.example.monew.domain.article.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CursorPageMapper {

    private final ArticleRepository articleRepository;

    public CursorPageResponseArticleDto toResponseDto (Slice<ArticleDto> articleList, String cursor, long totalElements) {
        ArticleDto lastContent = articleList.getContent().get(articleList.getSize() - 1);
        UUID lastContentId = lastContent.id();

        Instant nextAfter = articleRepository.findById(lastContentId).get().getCreatedAt();

        String nextCursor = switch (cursor) {
            case "publishDate" -> lastContent.publishDate().toString();
            case "commentCount" -> lastContent.commentCount().toString();
            case "viewCount" -> lastContent.viewCount().toString();
            default -> throw new IllegalArgumentException("Unsupported cursor: " + cursor);
        };

        return new CursorPageResponseArticleDto(
                articleList.getContent(),
                nextCursor,
                nextAfter,
                articleList.getSize(),
                totalElements,
                articleList.hasNext()
        );
    }

}
