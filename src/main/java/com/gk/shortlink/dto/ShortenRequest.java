package com.gk.shortlink.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

@Schema(description = "Request object for shortening a URL")
public record ShortenRequest(
    @NotBlank(message = "URL cannot be blank")
    @URL(message = "Invalid URL format")
    @Schema(description = "The original long URL to shorten", example = "https://www.google.com")
    String url
) {
}
