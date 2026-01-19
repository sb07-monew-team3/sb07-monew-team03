package com.example.monew.domain.article.service;

import com.example.monew.domain.article.client.naver.NaverNewsClient;
import com.example.monew.domain.article.entity.Article;
import com.example.monew.domain.article.mapper.NaverArticleMapper;
import com.example.monew.domain.article.repository.ArticleRepository;
import com.example.monew.domain.interest.entity.Interest;
import com.example.monew.domain.interest.entity.Keyword;
import com.example.monew.domain.interest.repository.InterestRepository;
import com.example.monew.domain.interest.repository.KeywordRepository;
import com.example.monew.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
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

    private final NotificationService notificationService;

    @Scheduled(fixedRate = BATCH_INTERVAL)
    @Transactional
    public void collectArticles() {


        // keyword와 interests 매핑
        Map<String, List<Interest>> map = new HashMap<>();

        List<Interest> interests = interestRepository.findAll();

        for (Interest interest : interests) {
            List<Keyword> keywords = keywordRepository.findAllByInterestId(interest.getId());

            // keyword이름을 가진 키가 없는 경우 빈 리스트를 생성
            // keyword이름을 가진 키가 있는 경우 리스트에 관심사 추가
            for (Keyword keyword : keywords) {
                map.computeIfAbsent(keyword.getKeyword(), k -> new ArrayList<>())
                        .add(interest);
            }
        }

        List<Article> articles = map.keySet().stream()
                .map(k ->
                        naverArticleMapper.toArticleList(naverNewsClient.search(k).items(), map.get(k))) // article 엔티티로 정제
                .flatMap(List::stream)
                .toList();

        // 링크 별 중복 기사를 HashMap의 형태로 저장
        Map<String, List<Article>> checkArticles = new HashMap<>();

        // key 값(기사 링크)이 존재하는 경우 value에 article 추가
        // key 값(기사 링크)이 없는 경우 빈 value로 빈 리스트 생성 후 article 추가
        articles.forEach(article ->
                checkArticles.
                        computeIfAbsent(article.getSourceUrl(), k -> new ArrayList<>())
                        .add(article)
        );

        List<Article> uniqueArticles = new ArrayList<>();

        checkArticles.forEach((k, v) -> {
            Article article = v.get(0);

            // 링크 별 중복된 기사들은 관심사만 더하여 하나의 기사로 병합
            for(int i = 1; i < v.size(); i++) {
                article.updateInterests(v.get(i).getInterests());
            }

            uniqueArticles.add(article);
        });

        Set<String> links = uniqueArticles.stream()
                .map(a -> a.getSourceUrl())
                .collect(Collectors.toSet());

        // 수집 된 기사 중 DB에 이미 있는 기사들 저장
        Map<String, Article> existingArticleMap =
                articleRepository.findAllBySourceUrlIn(links).stream()
                        .collect(Collectors.toMap(
                                Article::getSourceUrl, // key: article의 sourceUrl
                                Function.identity() // value: article 객체
                        ));

        List<Article> newArticles = new ArrayList<>();
        List<Article> existArticles = new ArrayList<>();

        for (Article article : uniqueArticles) {
            Article existing =  existingArticleMap.get(article.getSourceUrl());

            if (existing == null) {
                newArticles.add(article);
            } else {
                existing.updateInterests(article.getInterests());
                existArticles.add(existing);
            }

        }

        if(!newArticles.isEmpty()) {
            Collections.sort(newArticles, Comparator.comparing(Article::getPublishDate));

            Instant baseTime = Instant.now();
            for(int i = 0; i < newArticles.size(); i++) {
                newArticles.get(i).setSortTimestamp(
                        baseTime.plus(i, ChronoUnit.MILLIS)
                );
            }

            // 새로운 기사 등록시 구독 중인 관심사 관련 알림 생성
            HashMap<Interest, Integer> interestList = new HashMap<>();

            newArticles.forEach(article -> {
                List<Interest> articleInterests = article.getInterests();

                articleInterests.forEach(interest -> {
                    interestList.merge(interest, 1, Integer::sum);
                });
            });

            notificationService.createInterestAlarm(interestList);
            articleRepository.saveAll(newArticles);
        }

        if(!existArticles.isEmpty()) {
            articleRepository.saveAll(existArticles);
        }
    }
}
