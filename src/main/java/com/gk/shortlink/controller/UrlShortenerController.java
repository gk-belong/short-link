package com.gk.shortlink.controller;

import com.gk.shortlink.config.ShortLinkProperties;
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
    private final ShortLinkProperties properties;

    public UrlShortenerController(UrlShortenerService urlShortenerService, ShortLinkProperties properties) {
        this.urlShortenerService = urlShortenerService;
        this.properties = properties;
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
    public Mono<ResponseEntity<ShortenResponse>> shortenUrl(@Valid @RequestBody Mono<ShortenRequest> shortenRequestMono, ServerWebExchange exchange) {
        return shortenRequestMono.flatMap(shortenRequest ->
            urlShortenerService.shorten(shortenRequest.url())
                .map(code -> {
                    var baseUrl = getBaseUrl(exchange);
                    var shortUrl = baseUrl + "/" + code;
                    return ResponseEntity.status(HttpStatus.CREATED)
                        .body(new ShortenResponse(shortenRequest.url(), shortUrl, code));
                })
        );
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
        return urlShortenerService.getOriginalUrl(code)
            .switchIfEmpty(Mono.error(new UrlNotFoundException("Short code not found: " + code)))
            .map(originalUrl -> {
                var baseUrl = getBaseUrl(exchange);
                var shortUrl = baseUrl + "/" + code;
                return ResponseEntity.ok(new ShortenResponse(originalUrl, shortUrl, code));
            });
    }

    private String getBaseUrl(ServerWebExchange exchange) {
        String scheme = "http";
        if (exchange != null) {
            URI uri = exchange.getRequest().getURI();
            if (uri.getScheme() != null) {
                scheme = uri.getScheme();
            }
        }

        StringBuilder baseUrl = new StringBuilder(scheme).append("://").append(properties.host());

        if (exchange != null) {
            URI uri = exchange.getRequest().getURI();
            int port = uri.getPort();
            // Only append port if it's not the default for the scheme
            if (port != -1 && ((scheme.equals("http") && port != 80) || (scheme.equals("https") && port != 443))) {
                baseUrl.append(":").append(port);
            }
        }
        return baseUrl.toString();
    }
}
