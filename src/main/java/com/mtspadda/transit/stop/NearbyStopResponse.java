package com.mtspadda.transit.stop;

public record NearbyStopResponse(
        String id,
        String code,
        String name,
        double lat,
        double lon,
        long distanceMeters
) {

    static NearbyStopResponse from(NearbyStop stop){
        return new NearbyStopResponse(
                stop.getStopId(),
                stop.getStopCode(),
                stop.getStopName(),
                stop.getStopLat(),
                stop.getStopLon(),
                Math.round(stop.getDistanceMeters())
        );
    }
}
