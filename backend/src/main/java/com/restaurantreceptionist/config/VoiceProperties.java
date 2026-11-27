package com.restaurantreceptionist.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "voice")
public record VoiceProperties(Duration inactivityTimeout, Duration retentionPeriod, int cleanupBatchSize) {
}
