package com.example.monew.domain.article.client.rss;

import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;

import java.io.StringReader;

public class RssParser {
    public static SyndFeed parseRss(String xml) {
        try {
            return new SyndFeedInput().build(new StringReader(xml));
        } catch (Exception e) {
            throw new RuntimeException("RSS 파싱 실패", e);
        }
    }
}
