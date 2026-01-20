package com.example.monew.domain.article.client.rss;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
public class RssClient {

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public String fetch(String url) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("RSS fetch 실패", e);
        }
    }
}
