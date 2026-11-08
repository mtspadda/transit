package com.mtspadda.transit.realtime;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;

@ConfigurationProperties(prefix = "gtfs.realtime")
public record RealtimeProperties(boolean enabled, URI url, String apiKey) {
}
