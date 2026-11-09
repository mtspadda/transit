package com.mtspadda.transit.arrival;

import java.time.OffsetDateTime;

public record ArrivalResponse(
        String tripId,
        String route,
        String headsign,
        OffsetDateTime scheduledAt,
        OffsetDateTime expectedAt,
        Integer delaySeconds,
        ArrivalStatus status,
        long minutesAway
) {
}
