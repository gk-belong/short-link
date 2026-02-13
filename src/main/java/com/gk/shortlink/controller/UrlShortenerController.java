package com.gk.shortlink.controller;

import com.gk.shortlink.dto.ShortenRequest;
import com.gk.shortlink.dto.ShortenResponse;
import com.gk.shortlink.exception.UrlNotFoundException;
import com.gk.shortlink.service.UrlShortenerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/urls")
@Tag(name = "URL Shortener", description = "Endpoints for creating and managing short URLs")
public class UrlShortenerController {

    private final UrlShortenerService urlShortenerService;

    public UrlShortenerController(UrlShortenerService urlShortenerService) {
        this.urlShortenerService = urlShortenerService;
    }

    @PostMapping("/shorten")
    @Operation(
        summary = "Shorten a URL",
        description = "Takes a long URL and returns a unique short code and short URL",
        responses = {
            @ApiResponse(responseCode = "201", description = "URL successfully shortened", content = @Content(schema = @Schema(implementation = ShortenResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid URL format or blank URL")
        }
    )
    public Mono<ResponseEntity<ShortenResponse>> shortenUrl(@Valid @RequestBody ShortenRequest shortenRequest, ServerWebExchange exchange) {
        return Mono.fromCallable(() -> {
            var code = urlShortenerService.shorten(shortenRequest.url());
            var baseUrl = getBaseUrl(exchange);
            var shortUrl = (baseUrl != null ? baseUrl + "/" : "") + code;

            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ShortenResponse(shortenRequest.url(), shortUrl, code));
        });
    }

    @GetMapping("/{code}/info")
    @Operation(
        summary = "Get short code details",
        description = "Retrieves the original URL and other details for a given short code",
        responses = {
            @ApiResponse(responseCode = "200", description = "Details found", content = @Content(schema = @Schema(implementation = ShortenResponse.class))),
            @ApiResponse(responseCode = "404", description = "Short code not found")
        }
    )
    public Mono<ResponseEntity<ShortenResponse>> getShortCodeDetails(
        @Parameter(description = "The 6-character short code", example = "a1B2c3") @PathVariable String code,
        ServerWebExchange exchange) {
        return Mono.fromCallable(() -> {
            var originalUrl = urlShortenerService.getOriginalUrl(code);
            if (null == originalUrl) {
                throw new UrlNotFoundException("Short code not found: " + code);
            }
            var baseUrl = getBaseUrl(exchange);
            var shortUrl = (baseUrl != null ? baseUrl + "/" : "") + code;

            return ResponseEntity.ok(new ShortenResponse(originalUrl, shortUrl, code));
        });
    }

    private String getBaseUrl(ServerWebExchange exchange) {
        if (exchange == null) {
            return null;
        } else {
            exchange.getRequest().getURI();
        }
        URI uri = exchange.getRequest().getURI();
        String scheme = uri.getScheme();
        String host = uri.getHost();
        int port = uri.getPort();

        if (scheme == null || host == null) {
            return null;
        }

        // Handle short.ly specifically if needed, or just return the host
        StringBuilder baseUrl = new StringBuilder(scheme).append("://").append(host);

        // Only append port if it's not the default for the scheme
        if (port != -1 && ((scheme.equals("http") && port != 80) || (scheme.equals("https") && port != 443))) {
            baseUrl.append(":").append(port);
        }
        return baseUrl.toString();
    }
}
