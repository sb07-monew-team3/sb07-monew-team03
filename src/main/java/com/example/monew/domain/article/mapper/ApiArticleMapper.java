package com.example.monew.domain.article.mapper;

import com.example.monew.domain.article.client.naver.Item;
import com.example.monew.domain.article.dto.Source;
import com.example.monew.domain.article.entity.Article;
import com.example.monew.domain.interest.entity.Interest;
import com.example.monew.global.util.DateParser;
import com.rometools.rome.feed.synd.SyndEntry;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ApiArticleMapper {

    // Naver API의 item들을 기사 엔티티로 변환
    public List<Article> toArticleList(List<Item> items, List<Interest> interests)  {
        return items.stream()
                .map(i -> {

                        // 볼드체 제거
                        String summary = i.description().replaceAll("</?b>","");
                        String title = i.title().replaceAll("</?b>","");

                        return new Article(
                                        Source.NAVER.getValue(),
                                        i.originallink(),
                                        title,
                                        DateParser.parse(i.pubDate()),
                                        summary,
                                        false,
                                        null,
                                        interests
                                );
                        }
                )
                .toList();
    }

    // RSS API의 entry를 기사 엔티티로 변환
    public Article toArticle(SyndEntry entry, Source source) {

        String description = "";

        if(entry.getDescription() != null) {
            description = entry.getDescription().getValue();
        }

        return new Article(
                source.getValue(),
                entry.getLink(),
                entry.getTitle(),
                DateParser.parse(entry.getPublishedDate()),
                description,
                false,
                null,
                new ArrayList<>()
        );
    }
}
