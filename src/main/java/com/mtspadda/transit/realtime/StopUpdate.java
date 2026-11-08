package com.mtspadda.transit.realtime;

public record StopUpdate(
        Integer stopSequence,
        String stopId,
        Integer arrivalDelaySeconds,
        Long arrivalEpochSeconds,
        boolean skipped
) {
}
