package com.mtspadda.transit.realtime;

import java.time.Instant;
import java.util.Map;

public record RealtimeSnapshot(Instant feedTimestamp, Map<String, TripRealtime> trips) {

    static RealtimeSnapshot empty() {
        return new RealtimeSnapshot(Instant.EPOCH, Map.of());
    }
}