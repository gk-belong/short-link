package com.gk.shortlink.dto;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response object containing the shortened URL details")
public record ShortenResponse(
    @Schema(description = "The original long URL", example = "https://www.google.com")
    String originalUrl,
    @Schema(description = "The generated short URL", example = "http://localhost:8080/a1B2c3")
    String shortUrl,
    @Schema(description = "The 6-character unique short code", example = "a1B2c3")
    String code
) {
}
