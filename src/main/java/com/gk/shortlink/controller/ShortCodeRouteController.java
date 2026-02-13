package com.gk.shortlink.controller;

import com.gk.shortlink.exception.UrlNotFoundException;
import com.gk.shortlink.service.UrlShortenerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.net.URI;

@RestController
public class ShortCodeRouteController {

    public final UrlShortenerService urlShortenerService;

    public ShortCodeRouteController(UrlShortenerService urlShortenerService) {
        this.urlShortenerService = urlShortenerService;
    }

    @GetMapping("/{code}")
    public Mono<ResponseEntity<Void>> redirectToOriginalUrl(@PathVariable String code) {
        return Mono.fromCallable(() -> {
            String originalUrl = urlShortenerService.getOriginalUrl(code);
            if (originalUrl == null) {
                throw new UrlNotFoundException("Short code not found: " + code);
            }
            return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(originalUrl))
                .build();
        });
    }
}
