package com.gk.shortlink.service;

import com.gk.shortlink.config.ShortLinkProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.AbstractMap;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class UrlShortenerServiceTests {
    private static final int DEFAULT_CODE_LENGTH = 6;
    private UrlShortenerService urlShortenerService;

    @BeforeEach
    void setUp() {
        ShortLinkProperties properties = new ShortLinkProperties("short.ly", 10000, DEFAULT_CODE_LENGTH);
        urlShortenerService = new UrlShortenerService(properties);
    }

    @Test
    void shorten_ReturnsAlphanumericCodeOfCorrectLength() {
        String url = "https://example.com";
        StepVerifier.create(urlShortenerService.shorten(url))
            .assertNext(code -> {
                assertNotNull(code);
                assertEquals(DEFAULT_CODE_LENGTH, code.length());
                assertTrue(code.matches("^[a-zA-Z0-9]+$"), "Code should be alphanumeric");
            })
            .verifyComplete();
    }

    @Test
    void shorten_ReturnsSameCodeForSameUrl() {
        String url = "https://example.com";

        urlShortenerService.shorten(url)
            .flatMap(code1 -> urlShortenerService.shorten(url)
                .doOnNext(code2 -> assertEquals(code1, code2, "Same URL should return the same code")))
            .as(StepVerifier::create)
            .expectNextCount(1)
            .verifyComplete();
    }

    @Test
    void shorten_ReturnsDifferentCodesForDifferentUrls() {
        String url1 = "https://example.com/1";
        String url2 = "https://example.com/2";

        urlShortenerService.shorten(url1)
            .flatMap(code1 -> urlShortenerService.shorten(url2)
                .doOnNext(code2 -> assertNotEquals(code1, code2, "Different URLs should return different codes")))
            .as(StepVerifier::create)
            .expectNextCount(1)
            .verifyComplete();
    }

    @Test
    void shorten_HandlesConcurrentRequestsForSameUrl() {
        String url = "https://example.com/concurrent";
        int threadCount = 50;

        Flux.range(0, threadCount)
            .flatMap(i -> urlShortenerService.shorten(url))
            .collect(Collectors.toSet())
            .as(StepVerifier::create)
            .assertNext(codes -> assertEquals(1, codes.size(), "All threads should receive the same code for the same URL"))
            .verifyComplete();
    }

    @Test
    void getOriginalUrl_ReturnsOriginalUrlAfterShorten() {
        String url = "https://example.com";

        urlShortenerService.shorten(url)
            .flatMap(code -> urlShortenerService.getOriginalUrl(code))
            .as(StepVerifier::create)
            .expectNext(url)
            .verifyComplete();
    }

    @Test
    void shorten_PopulatesCodeToUrlCacheConcurrently() {
        int count = 100;

        Flux.range(0, count)
            .flatMap(i -> {
                String url = "https://example.com/" + i;
                return urlShortenerService.shorten(url)
                    .map(code -> new AbstractMap.SimpleEntry<>(code, url));
            })
            .collectList()
            .flatMapIterable(list -> list)
            .flatMap(entry -> urlShortenerService.getOriginalUrl(entry.getKey())
                .doOnNext(originalUrl -> assertEquals(entry.getValue(), originalUrl, "Cache should be populated for " + entry.getValue())))
            .as(StepVerifier::create)
            .expectNextCount(count)
            .verifyComplete();
    }

    @Test
    void getOriginalUrl_ReturnsEmptyForNonExistentCode() {
        StepVerifier.create(urlShortenerService.getOriginalUrl("nonexistent"))
            .expectNextCount(0)
            .verifyComplete();
    }
}
