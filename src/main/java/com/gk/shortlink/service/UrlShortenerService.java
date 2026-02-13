package com.gk.shortlink.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

@Service
public class UrlShortenerService {

    public static final int CODE_LENGTH = 6;
    // Configuration should be managed via application properties - future change
    private static final int MAX_CAPACITY = 10000;
    private final Cache<String, String> urlToCodeCache;
    private final Cache<String, String> codeToUrlCache;

    // Using caffeine cache to set boundaries Vs concurrentHashMap
    // Note: For production applications Redis or another distributed system approach is advisable
    public UrlShortenerService() {
        this.urlToCodeCache = Caffeine.newBuilder()
            .maximumSize(MAX_CAPACITY)
            .build();

        this.codeToUrlCache = Caffeine.newBuilder()
            .maximumSize(MAX_CAPACITY)
            .build();
    }

    /**
     * Method to create a short code for a given original URL conditionally
     * in a thread-safe manner and to populate code to url cache to support
     * a later short code-specific journey such as route to original URL
     * or to retrieve short code details
     *
     * @param originalUrl URL to be shortened
     * @return short code as String
     */
    public String shorten(String originalUrl) {
        return urlToCodeCache.get(originalUrl, k -> {
            String code = generateCode();
            codeToUrlCache.put(code, originalUrl);
            return code;
        });
    }

    /**
     * Method to retrieve the associated URL for a short code
     *
     * @param code short code representing the URL
     * @return original URL as String
     */
    public String getOriginalUrl(String code) {
        return codeToUrlCache.getIfPresent(code);
    }

    private String generateCode() {
        return RandomStringUtils.secureStrong().nextAlphanumeric(CODE_LENGTH);
    }
}
