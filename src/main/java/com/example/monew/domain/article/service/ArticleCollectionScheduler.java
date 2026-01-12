package com.example.monew.domain.article.service;

import com.example.monew.domain.article.client.naver.NaverNewsClient;
import com.example.monew.domain.article.entity.Article;
import com.example.monew.domain.article.mapper.NaverArticleMapper;
import com.example.monew.domain.article.repository.ArticleRepository;
import com.example.monew.domain.interest.entity.Interest;
import com.example.monew.domain.interest.entity.Keyword;
import com.example.monew.domain.interest.repository.InterestRepository;
import com.example.monew.domain.interest.repository.KeywordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ArticleCollectionScheduler {

    static final long BATCH_INTERVAL = 1000 * 60 * 60;

    private final ArticleRepository articleRepository;
    private final InterestRepository interestRepository;
    private final KeywordRepository keywordRepository;

    private final NaverArticleMapper naverArticleMapper;

    private final NaverNewsClient naverNewsClient;

    @Scheduled(fixedRate = BATCH_INTERVAL)
    public void collectArticles() {

        /*
        1. 현재 존재하는 관심사의 모든 키워드를 저장
        2. key(키워드), value(관심사) 형태로 HashMap 저장
        3. 키워드 기준으로 기사 검색 (naver 등)
        4. 반환된 item을 article로 변환하면서 interests에 HashMap의 value를 집어넣는다.
        5. 이미 저장된 기사는 제외하고 새로운 기사를 db에 저장
         */

        HashMap<String, List<Interest>> map = new HashMap<>();

        List<Interest> interests = interestRepository.findAll();

        interests.forEach(interest -> {
            List<Keyword> keywords = keywordRepository.findAllByInterestId(interest.getId());

            keywords.forEach(k -> {
                String keywordName = k.getKeyword();

                List<Interest> value = map.get(keywordName);

                if(value == null) {
                    map.put(keywordName, List.of(interest));
                } else {
                    value.add(interest);
                    map.put(keywordName, value);
                }
            });
        });

        List<Article> articles = map.keySet().stream()
                .map(k ->
                        naverArticleMapper.toArticleList(naverNewsClient.search(k).items(), map.get(k))) // article 엔티티로 정제
                .flatMap(List::stream)
                .toList();

        Set<String> links = articles.stream()
                .map(a -> a.getSourceUrl())
                .collect(Collectors.toSet());

        Set<String> existingLinks = articleRepository.findAllBySourceUrlIn(links).stream()
                .map(a -> a.getSourceUrl())
                .collect(Collectors.toSet());

        List<Article> newArticles = articles.stream()
                .filter(a -> !existingLinks.contains(a.getSourceUrl()))
                .toList();

        if(!newArticles.isEmpty()) {
            articleRepository.saveAll(newArticles);
        }
    }
}
