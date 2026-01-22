package com.example.monew.domain.article.service;

import com.example.monew.domain.article.client.naver.NaverNewsClient;
import com.example.monew.domain.article.client.rss.ArticleRefiner;
import com.example.monew.domain.article.client.rss.RssClient;
import com.example.monew.domain.article.client.rss.RssParser;
import com.example.monew.domain.article.dto.Source;
import com.example.monew.domain.article.entity.Article;
import com.example.monew.domain.article.mapper.ApiArticleMapper;
import com.example.monew.domain.article.repository.ArticleRepository;
import com.example.monew.domain.interest.entity.Interest;
import com.example.monew.domain.interest.entity.Keyword;
import com.example.monew.domain.interest.repository.InterestRepository;
import com.example.monew.domain.interest.repository.KeywordRepository;
import com.example.monew.domain.notification.service.NotificationService;
import com.rometools.rome.feed.synd.SyndFeed;
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

    private final ApiArticleMapper apiArticleMapper;

    private final NaverNewsClient naverNewsClient;

    private final NotificationService notificationService;

    private final RssClient rssClient;

    @Transactional
    public void collectArticles() {

        // RSS URL들
        Map<Source, String> urls = new LinkedHashMap<>();
        urls.put(Source.HANKYUNG, "https://www.hankyung.com/feed/all-news");
        urls.put(Source.CHOSUN, "https://www.chosun.com/arc/outboundfeeds/rss/?outputType=xml");
        urls.put(Source.YEONHAP,"https://www.yonhapnewstv.co.kr/browse/feed/");


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

        // Naver API로 검색
        List<Article> articles = map.keySet().stream()
                .map(k ->
                        apiArticleMapper.toArticleList(naverNewsClient.search(k).items(), map.get(k))) // article 엔티티로 정제
                .flatMap(List::stream)
                .collect(Collectors.toList());

        // RSS의 최신 기사 저장
        List<Article> rssArticles = new ArrayList<>();

        urls.forEach((source,url) -> {
            String xml = rssClient.fetch(url);
            SyndFeed originalArticles = RssParser.parseRss(xml);
            List<Article> articleList = originalArticles.getEntries().stream()
                    .map(entry -> apiArticleMapper.toArticle(entry, source))
                    .toList();
            rssArticles.addAll(articleList);
        });

        // RSS의 기사를 검색어 기준으로 필터링
        List<Article> refinedRssArticles = ArticleRefiner.refineArticles(rssArticles, map);

        // 수집된 모든 기사를 하나의 리스트에 저장
        Collections.addAll(articles, refinedRssArticles.toArray(new Article[0]));

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
