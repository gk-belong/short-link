package com.gk.shortlink.dto;

public record ShortenResponse(
    String originalUrl,
    String shortUrl,
    String code
) {
}
