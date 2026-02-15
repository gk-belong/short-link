package com.gk.shortlink.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "shortlink")
public record ShortLinkProperties(
    @DefaultValue("short.ly") String host,
    @DefaultValue("10000") int maxCapacity,
    @DefaultValue("6") int codeLength
) {}
