package com.example.monew.domain.article.client.rss;

import com.example.monew.domain.article.entity.Article;
import com.example.monew.domain.interest.entity.Interest;

import java.util.*;

public class ArticleRefiner {
    public static List<Article> refineArticles(List<Article> articles, Map<String, List<Interest>> interests) {
        List<Article> result = new ArrayList<>();

        for (Article article : articles) {
            Set<Interest> matched = new HashSet<>();

            String title = article.getTitle();
            String summary = article.getSummary();

            for (Map.Entry<String, List<Interest>> entry : interests.entrySet()) {
                String keyword = entry.getKey();

                // RSS로 수집된 기사 중 제목과 요약에 키워드를 포함한 기사만 리스트에 저장
                if (title.contains(keyword)
                        || (summary != null && summary.contains(keyword))) {
                    matched.addAll(entry.getValue());
                }
            }

            if (!matched.isEmpty()) {
                // 키워드를 포함한 기사는 관심사 연결
                article.updateInterests(new ArrayList<>(matched));
                result.add(article);
            }
        }

        return result;
    }
}
