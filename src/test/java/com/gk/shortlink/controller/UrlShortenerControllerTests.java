package com.gk.shortlink.controller;

import com.gk.shortlink.config.ShortLinkProperties;
import com.gk.shortlink.dto.ShortenRequest;
import com.gk.shortlink.dto.ShortenResponse;
import com.gk.shortlink.service.UrlShortenerService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;

@WebFluxTest(controllers = UrlShortenerController.class, properties = "shortlink.host=short.ly")
class UrlShortenerControllerTests {

    private static final String ORIGINAL_URL = "https://example.com";

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private UrlShortenerService urlShortenerService;

    @MockitoBean
    private ShortLinkProperties shortLinkProperties;

    @Test
    void shortenUrl_ReturnsCreatedResponse() {
        String code = "a1B2c3";
        Mockito.when(urlShortenerService.shorten(ORIGINAL_URL)).thenReturn(Mono.just(code));
        Mockito.when(shortLinkProperties.host()).thenReturn("short.ly");

        ShortenRequest request = new ShortenRequest(ORIGINAL_URL);

        webTestClient.post()
            .uri("/api/v1/urls/shorten")
            .bodyValue(request)
            .exchange()
            .expectStatus().isCreated()
            .expectBody(ShortenResponse.class)
            .value(response -> {
                assertEquals(ORIGINAL_URL, response.originalUrl());
                assertEquals("http://short.ly/" + code, response.shortUrl());
                assertEquals(code, response.code());
            });
    }

    @Test
    void shortenUrl_ReturnsBadRequest_WhenUrlIsBlank() {
        ShortenRequest request = new ShortenRequest("");

        webTestClient.post()
            .uri("/api/v1/urls/shorten")
            .bodyValue(request)
            .exchange()
            .expectStatus().isBadRequest();
    }

    @Test
    void shortenUrl_ReturnsBadRequest_WhenUrlIsInvalid() {
        ShortenRequest request = new ShortenRequest("not-a-url");

        webTestClient.post()
            .uri("/api/v1/urls/shorten")
            .bodyValue(request)
            .exchange()
            .expectStatus().isBadRequest();
    }

    @Test
    void shortenUrl_ReturnsNotFound_WhenInvalidEndpointIsUsed() {
        ShortenRequest request = new ShortenRequest(ORIGINAL_URL);

        webTestClient.post()
            .uri("/api/v1/urls/invalid-endpoint")
            .bodyValue(request)
            .exchange()
            .expectStatus().isNotFound();
    }

    @Test
    void getShortCodeDetails_ReturnsOkResponse_WhenCodeExists() {
        String code = "a1B2c3";
        Mockito.when(urlShortenerService.getOriginalUrl(code)).thenReturn(Mono.just(ORIGINAL_URL));
        Mockito.when(shortLinkProperties.host()).thenReturn("short.ly");

        webTestClient.get()
            .uri("/api/v1/urls/{code}/info", code)
            .exchange()
            .expectStatus().isOk()
            .expectBody(ShortenResponse.class)
            .value(response -> {
                assertEquals(ORIGINAL_URL, response.originalUrl());
                assertEquals("http://short.ly/" + code, response.shortUrl());
                assertEquals(code, response.code());
            });
    }

    @Test
    void getShortCodeDetails_ReturnsNotFound_WhenCodeDoesNotExist() {
        String code = "nonexistent";
        Mockito.when(urlShortenerService.getOriginalUrl(code)).thenReturn(Mono.empty());

        webTestClient.get()
            .uri("/api/v1/urls/{code}/info", code)
            .exchange()
            .expectStatus().isNotFound();
    }
}
