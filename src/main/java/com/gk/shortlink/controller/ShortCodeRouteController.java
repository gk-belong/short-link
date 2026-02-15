package com.gk.shortlink.controller;

import com.gk.shortlink.exception.UrlNotFoundException;
import com.gk.shortlink.service.UrlShortenerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.net.URI;

@RestController
@Tag(name = "Redirection", description = "Endpoint for redirecting short codes to original URLs")
public class ShortCodeRouteController {

    public final UrlShortenerService urlShortenerService;

    public ShortCodeRouteController(UrlShortenerService urlShortenerService) {
        this.urlShortenerService = urlShortenerService;
    }

    @GetMapping("/{code:[a-zA-Z0-9]{6}}")
    @Operation(
        summary = "Redirect to original URL",
        description = "Redirects the client to the original long URL associated with the short code",
        responses = {
            @ApiResponse(responseCode = "302", description = "Redirection to original URL"),
            @ApiResponse(responseCode = "404", description = "Short code not found")
        }
    )
    public Mono<ResponseEntity<Void>> redirectToOriginalUrl(
        @Parameter(description = "The 6-character short code", example = "a1B2c3") @PathVariable String code) {
        return urlShortenerService.getOriginalUrl(code)
            .switchIfEmpty(Mono.error(new UrlNotFoundException("Short code not found: " + code)))
            .map(originalUrl -> ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(originalUrl))
                .build());
    }
}
