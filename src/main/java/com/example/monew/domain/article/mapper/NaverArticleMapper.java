package com.example.monew.domain.article.mapper;

import com.example.monew.domain.article.client.naver.Item;
import com.example.monew.domain.article.dto.Source;
import com.example.monew.domain.article.entity.Article;
import com.example.monew.domain.interest.entity.Interest;
import com.example.monew.global.util.DateParser;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NaverArticleMapper {
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
}
