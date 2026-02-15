package com.gk.shortlink.service;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.gk.shortlink.config.ShortLinkProperties;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;

@Service
public class UrlShortenerService {

    private final AsyncCache<String, String> urlToCodeCache;
    private final AsyncCache<String, String> codeToUrlCache;
    private final ShortLinkProperties properties;

    // Using caffeine cache to set boundaries Vs concurrentHashMap
    // Note: For production applications Redis or another distributed system approach is advisable
    public UrlShortenerService(ShortLinkProperties properties) {
        this.properties = properties;
        this.urlToCodeCache = Caffeine.newBuilder()
            .maximumSize(properties.maxCapacity())
            .buildAsync();

        this.codeToUrlCache = Caffeine.newBuilder()
            .maximumSize(properties.maxCapacity())
            .buildAsync();
    }

    /**
     * Method to create a short code for a given original URL conditionally
     * in a thread-safe manner and to populate code to url cache to support
     * a later short code-specific journey such as route to original URL
     * or to retrieve short code details
     *
     * @param originalUrl URL to be shortened
     * @return short code as String wrapped in Mono
     */
    public Mono<String> shorten(String originalUrl) {
        return Mono.fromFuture(() -> urlToCodeCache.get(originalUrl, (k, executor) -> {
            String code = generateCode();
            codeToUrlCache.put(code, CompletableFuture.completedFuture(originalUrl));
            return CompletableFuture.completedFuture(code);
        }));
    }

    /**
     * Method to retrieve the associated URL for a short code
     *
     * @param code short code representing the URL
     * @return original URL as String wrapped in Mono
     */
    public Mono<String> getOriginalUrl(String code) {
        return Mono.justOrEmpty(codeToUrlCache.getIfPresent(code))
            .flatMap(future -> future != null ? Mono.fromFuture(future) : Mono.empty());
    }

    private String generateCode() {
        return RandomStringUtils.secureStrong().nextAlphanumeric(properties.codeLength());
    }
}
