package com.mtspadda.transit.stop;

public interface NearbyStop {
    String getStopId();
    String getStopCode();
    String getStopName();
    Double getStopLat();
    Double getStopLon();
    Double getDistanceMeters();
}
