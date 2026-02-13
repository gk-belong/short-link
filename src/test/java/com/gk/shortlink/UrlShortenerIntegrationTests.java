package com.gk.shortlink;

import com.gk.shortlink.dto.ShortenRequest;
import com.gk.shortlink.dto.ShortenResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UrlShortenerIntegrationTests {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void testCompleteFlow() {
        String originalUrl = "https://www.google.com";
        ShortenRequest shortenRequest = new ShortenRequest(originalUrl);

        // 1. Shorten URL
        ShortenResponse shortenResponse = webTestClient.post()
            .uri("/api/v1/urls/shorten")
            .bodyValue(shortenRequest)
            .exchange()
            .expectStatus().isCreated()
            .expectBody(ShortenResponse.class)
            .returnResult()
            .getResponseBody();

        assertNotNull(shortenResponse);
        String code = shortenResponse.code();
        assertNotNull(code);
        assertEquals(originalUrl, shortenResponse.originalUrl());

        // 2. Get info
        webTestClient.get()
            .uri("/api/v1/urls/{code}/info", code)
            .exchange()
            .expectStatus().isOk()
            .expectBody(ShortenResponse.class)
            .value(response -> {
                assertEquals(originalUrl, response.originalUrl());
                assertEquals(code, response.code());
            });

        // 3. Redirect
        webTestClient.get()
            .uri("/{code}", code)
            .exchange()
            .expectStatus().isFound()
            .expectHeader().valueEquals("Location", originalUrl);
    }

    @Test
    void testShortenInvalidUrl_ReturnsBadRequest() {
        ShortenRequest invalidRequest = new ShortenRequest("not-a-url");

        webTestClient.post()
            .uri("/api/v1/urls/shorten")
            .bodyValue(invalidRequest)
            .exchange()
            .expectStatus().isBadRequest();
    }

    @Test
    void testGetInfo_NonExistentCode_ReturnsNotFound() {
        webTestClient.get()
            .uri("/api/v1/urls/nonexistent/info")
            .exchange()
            .expectStatus().isNotFound();
    }

    @Test
    void testRedirect_NonExistentCode_ReturnsNotFound() {
        webTestClient.get()
            .uri("/nonexistent")
            .exchange()
            .expectStatus().isNotFound();
    }
}
