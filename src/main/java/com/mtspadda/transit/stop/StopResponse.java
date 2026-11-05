package com.mtspadda.transit.stop;

public record StopResponse(String id, String code, String name, double lat, double lon) {
    static StopResponse from(Stops stop) {
        return new StopResponse(stop.getStopId(), stop.getStopCode(), stop.getStopName(),
                stop.getStopLat(), stop.getStopLon());
    }
}

