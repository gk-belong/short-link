package com.gk.shortlink.controller;

import com.gk.shortlink.service.UrlShortenerService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest(ShortCodeRouteController.class)
class ShortCodeRouteControllerTests {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private UrlShortenerService urlShortenerService;

    @Test
    void redirectToOriginalUrl_RedirectsToOriginalUrl_WhenCodeExists() {
        String code = "abc123";
        String originalUrl = "https://example.com";
        Mockito.when(urlShortenerService.getOriginalUrl(code)).thenReturn(originalUrl);

        webTestClient.get()
            .uri("/{code}", code)
            .exchange()
            .expectStatus().isFound()
            .expectHeader().location(originalUrl);
    }

    @Test
    void redirectToOriginalUrl_ReturnsNotFound_WhenCodeDoesNotExist() {
        String code = "missing";
        Mockito.when(urlShortenerService.getOriginalUrl(code)).thenReturn(null);

        webTestClient.get()
            .uri("/{code}", code)
            .exchange()
            .expectStatus().isNotFound();
    }
}
