package com.mtspadda.transit.arrival;

import java.time.OffsetDateTime;

public record ArrivalResponse(
        String tripId,
        String route,
        String headsign,
        OffsetDateTime scheduledAt,
        long minutesAway
) {
}
