package com.mtspadda.transit.realtime;

import java.util.List;

public record TripRealtime(
        String stopId,
        boolean canceled,
        List<StopUpdate> stopUpdates
) {
}
