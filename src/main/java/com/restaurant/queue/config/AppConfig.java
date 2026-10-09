package com.restaurant.queue.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Centralised application configuration loaded from application.properties.
 * Avoids hard-coded business values throughout the codebase.
 */
@Configuration
@ConfigurationProperties(prefix = "app.queue")
public class AppConfig {

    /** Average minutes a party spends at a table (used for EWT calculation). */
    private int avgServiceTimeMinutes = 5;

    /** Maximum allowed party size when joining the queue. */
    private int maxPartySize = 10;

    public int getAvgServiceTimeMinutes() {
        return avgServiceTimeMinutes;
    }

    public void setAvgServiceTimeMinutes(int avgServiceTimeMinutes) {
        this.avgServiceTimeMinutes = avgServiceTimeMinutes;
    }

    public int getMaxPartySize() {
        return maxPartySize;
    }

    public void setMaxPartySize(int maxPartySize) {
        this.maxPartySize = maxPartySize;
    }
}
