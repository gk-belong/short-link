package com.gk.shortlink.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.gk.shortlink.service.UrlShortenerService.CODE_LENGTH;
import static org.junit.jupiter.api.Assertions.*;

class UrlShortenerServiceTests {

    private UrlShortenerService urlShortenerService;

    @BeforeEach
    void setUp() {
        urlShortenerService = new UrlShortenerService();
    }

    @Test
    void shorten_ReturnsAlphanumericCodeOfCorrectLength() {
        String url = "https://example.com";
        String code = urlShortenerService.shorten(url);

        assertNotNull(code);
        assertEquals(CODE_LENGTH, code.length());
        assertTrue(code.matches("^[a-zA-Z0-9]+$"), "Code should be alphanumeric");
    }

    @Test
    void shorten_ReturnsSameCodeForSameUrl() {
        String url = "https://example.com";
        String code1 = urlShortenerService.shorten(url);
        String code2 = urlShortenerService.shorten(url);

        assertEquals(code1, code2, "Same URL should return the same code");
    }

    @Test
    void shorten_ReturnsDifferentCodesForDifferentUrls() {
        String url1 = "https://example.com/1";
        String url2 = "https://example.com/2";
        String code1 = urlShortenerService.shorten(url1);
        String code2 = urlShortenerService.shorten(url2);

        assertNotEquals(code1, code2, "Different URLs should return different codes");
    }

    @Test
    void shorten_HandlesConcurrentRequestsForSameUrl() throws InterruptedException {
        String url = "https://example.com/concurrent";
        int threadCount = 50;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(threadCount);
        Set<String> codes = Collections.newSetFromMap(new ConcurrentHashMap<>());

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    startLatch.await();
                    String code = urlShortenerService.shorten(url);
                    codes.add(code);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        finishLatch.await();
        executorService.shutdown();

        assertEquals(1, codes.size(), "All threads should receive the same code for the same URL");
    }

    @Test
    void getOriginalUrl_ReturnsOriginalUrlAfterShorten() {
        String url = "https://example.com";
        String code = urlShortenerService.shorten(url);
        String originalUrl = urlShortenerService.getOriginalUrl(code);

        assertEquals(url, originalUrl, "getOriginalUrl should return the original URL after shorten");
    }

    @Test
    void shorten_PopulatesCodeToUrlCacheConcurrently() throws InterruptedException {
        int count = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(count);
        ConcurrentHashMap<String, String> results = new ConcurrentHashMap<>();

        for (int i = 0; i < count; i++) {
            final int index = i;
            executorService.submit(() -> {
                try {
                    String url = "https://example.com/" + index;
                    String code = urlShortenerService.shorten(url);
                    results.put(code, url);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        assertEquals(count, results.size());
        results.forEach((code, url) ->
            assertEquals(url, urlShortenerService.getOriginalUrl(code), "Cache should be populated for " + url));
    }
}
